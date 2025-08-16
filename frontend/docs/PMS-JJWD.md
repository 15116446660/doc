# PMS系统与企业文档协作平台集成技术方案

## 1. 需求背景分析

### 1.1 系统现状
- **PMS系统**：基于LDAP认证的项目管理系统
- **企业文档协作平台（JJWD）**：自建用户体系，数据结构为"分类 -> 项目 -> 文档"的层级关系

### 1.2 核心需求
1. PMS系统需要唤起JJWD平台的特定功能页面
2. 用户在PMS中已认证，访问JJWD时无需重新登录
3. 需要传递用户身份、权限和业务上下文
4. 确保数据层级权限的正确映射

### 1.3 原方案问题分析

#### 1.3.1 APP Key认证方案的局限性
- **用户身份缺失**：单纯的APP Key无法传递具体用户信息
- **权限粒度不足**：无法实现用户级别的细粒度权限控制
- **安全风险**：前端直接使用APP Key存在泄露风险

#### 1.3.2 前端状态管理问题
- **状态冲突**：Vue3无状态管理，跨系统状态同步困难
- **认证冲突**：localStorage token检查与跨系统认证冲突
- **拦截问题**：401拦截机制阻止跨系统访问

### 1.4 技术挑战
1. **身份认证同步**：LDAP认证与自建用户体系的对接
2. **权限映射**：PMS权限与JJWD权限的映射关系
3. **数据层级控制**：分类、项目、文档的访问权限控制
4. **前端状态管理**：Vue3无状态管理下的跨系统状态同步

## 2. 整体架构设计

### 2.1 技术架构图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   PMS系统       │    │   认证网关       │    │   JJWD平台      │
│  (LDAP认证)     │    │  (JWT+APP Key)  │    │  (自建用户体系)  │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │   前端      │ │◄──►│ │  Token服务  │ │◄──►│ │   前端      │ │
│ │  (Vue3)     │ │    │ │             │ │    │ │  (Vue3)     │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │  后端       │ │◄──►│ │  权限映射   │ │◄──►│ │   后端      │ │
│ │ (Spring)    │ │    │ │   服务      │ │    │ │ (Spring)    │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │   LDAP服务器     │
                    │  (统一用户源)    │
                    └─────────────────┘
```

### 2.2 核心设计原则

1. **安全第一**：所有跨系统通信使用HTTPS + JWT签名
2. **无状态设计**：使用JWT Token传递用户状态，避免Session依赖
3. **权限最小化**：用户只能访问其在PMS中有权限的资源
4. **可扩展性**：支持未来更多系统的接入

## 3. 认证授权方案设计

### 3.1 混合认证模式

采用 **JWT Token + APP Key** 的混合认证模式：

- **APP Key**：用于系统级别的身份认证，验证请求来源的合法性
- **JWT Token**：用于用户级别的身份和权限传递，包含完整的用户信息和权限

### 3.2 Token结构设计

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "iss": "PMS-System",
    "sub": "user123",
    "aud": "JJWD-Platform",
    "exp": 1640995200,
    "iat": 1640991600,
    "jti": "unique-token-id",
    "app_key": "pms_app_key_12345",
    "user_info": {
      "username": "zhangsan",
      "email": "zhangsan@company.com",
      "display_name": "张三",
      "department": "研发部",
      "employee_id": "EMP001",
      "ldap_groups": ["cn=研发部,ou=Groups,dc=example,dc=com"]
    },
    "permissions": {
      "categories": ["CAT001", "CAT002"],
      "projects": ["PROJ001", "PROJ002"],
      "documents": ["DOC001", "DOC002"],
      "actions": ["READ", "EDIT", "CREATE"]
    },
    "context": {
      "target_page": "/document/edit/123",
      "category_id": "CAT001",
      "project_id": "PROJ001",
      "document_id": "DOC123"
    }
  }
}
```

### 3.3 权限映射配置

```yaml
# 权限映射配置
permission-mapping:
  # PMS角色 -> JJWD角色
  role-mapping:
    "PROJECT_MANAGER": "DOCUMENT_ADMIN"
    "DEVELOPER": "DOCUMENT_EDITOR"
    "TESTER": "DOCUMENT_VIEWER"
    "ADMIN": "SYSTEM_ADMIN"
  
  # PMS权限 -> JJWD权限
  action-mapping:
    "PROJECT_READ": "DOCUMENT_READ"
    "PROJECT_EDIT": "DOCUMENT_EDIT"
    "PROJECT_CREATE": "DOCUMENT_CREATE"
    "PROJECT_DELETE": "DOCUMENT_DELETE"
  
  # 数据层级映射
  data-mapping:
    # PMS项目 -> JJWD分类/项目
    project-to-category: true
    inherit-permissions: true
```

## 4. 后端实现方案

### 4.1 PMS系统后端实现

#### 4.1.1 跨系统认证服务

```java
@Service
@Slf4j
public class CrossSystemAuthService {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${cross-system.jjwd.app-key}")
    private String jjwdAppKey;
    
    @Value("${cross-system.jjwd.base-url}")
    private String jjwdBaseUrl;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PermissionMappingService permissionMappingService;
    
    /**
     * 生成跨系统访问Token
     */
    public CrossSystemToken generateCrossSystemToken(String username, CrossSystemRequest request) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("用户不存在: " + username);
        }
        
        // 构建用户信息
        Map<String, Object> userInfo = buildUserInfo(user);
        
        // 构建权限信息
        Map<String, Object> permissions = buildPermissions(user, request);
        
        // 构建上下文信息
        Map<String, Object> context = buildContext(request);
        
        // 生成JWT Token
        String token = Jwts.builder()
            .setIssuer("PMS-System")
            .setSubject(username)
            .setAudience("JJWD-Platform")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1小时
            .setId(UUID.randomUUID().toString())
            .claim("app_key", jjwdAppKey)
            .claim("user_info", userInfo)
            .claim("permissions", permissions)
            .claim("context", context)
            .signWith(SignatureAlgorithm.HS256, jwtSecret)
            .compact();
        
        // 构建跳转URL
        String redirectUrl = buildRedirectUrl(request.getTargetPage(), token);
        
        return CrossSystemToken.builder()
            .token(token)
            .redirectUrl(redirectUrl)
            .expiresIn(3600)
            .build();
    }
    
    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("display_name", user.getDisplayName());
        userInfo.put("department", user.getDepartment());
        userInfo.put("employee_id", user.getEmployeeId());
        userInfo.put("ldap_groups", user.getLdapGroups());
        return userInfo;
    }
    
    private Map<String, Object> buildPermissions(User user, CrossSystemRequest request) {
        // 获取用户在PMS中的权限
        List<String> pmsRoles = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());
        
        List<String> pmsPermissions = user.getPermissions().stream()
            .map(Permission::getName)
            .collect(Collectors.toList());
        
        // 映射到JJWD权限
        List<String> jjwdRoles = permissionMappingService.mapRoles(pmsRoles);
        List<String> jjwdActions = permissionMappingService.mapActions(pmsPermissions);
        
        // 根据请求上下文确定数据权限
        List<String> categories = getAccessibleCategories(user, request);
        List<String> projects = getAccessibleProjects(user, request);
        List<String> documents = getAccessibleDocuments(user, request);
        
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("roles", jjwdRoles);
        permissions.put("actions", jjwdActions);
        permissions.put("categories", categories);
        permissions.put("projects", projects);
        permissions.put("documents", documents);
        
        return permissions;
    }
    
    private Map<String, Object> buildContext(CrossSystemRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("target_page", request.getTargetPage());
        context.put("category_id", request.getCategoryId());
        context.put("project_id", request.getProjectId());
        context.put("document_id", request.getDocumentId());
        context.put("action", request.getAction());
        return context;
    }
    
    private String buildRedirectUrl(String targetPage, String token) {
        return String.format("%s/auth/cross-system-login?token=%s&redirect=%s",
            jjwdBaseUrl, token, URLEncoder.encode(targetPage, StandardCharsets.UTF_8));
    }
}
```

#### 4.1.2 权限映射服务

```java
@Service
@ConfigurationProperties(prefix = "permission-mapping")
@Data
public class PermissionMappingService {

    private Map<String, String> roleMapping = new HashMap<>();
    private Map<String, String> actionMapping = new HashMap<>();

    @Autowired
    private ProjectService projectService;

    /**
     * 映射PMS角色到JJWD角色
     */
    public List<String> mapRoles(List<String> pmsRoles) {
        return pmsRoles.stream()
            .map(role -> roleMapping.getOrDefault(role, role))
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 映射PMS权限到JJWD权限
     */
    public List<String> mapActions(List<String> pmsPermissions) {
        return pmsPermissions.stream()
            .map(permission -> actionMapping.getOrDefault(permission, permission))
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 获取用户可访问的分类列表
     */
    public List<String> getAccessibleCategories(User user, CrossSystemRequest request) {
        // 根据用户在PMS中的项目权限，映射到JJWD的分类权限
        List<String> userProjects = user.getProjects().stream()
            .map(Project::getId)
            .collect(Collectors.toList());

        // 这里需要根据业务逻辑实现PMS项目到JJWD分类的映射
        return mapProjectsToCategories(userProjects);
    }

    /**
     * 获取用户可访问的项目列表
     */
    public List<String> getAccessibleProjects(User user, CrossSystemRequest request) {
        return user.getProjects().stream()
            .map(Project::getId)
            .collect(Collectors.toList());
    }

    /**
     * 获取用户可访问的文档列表
     */
    public List<String> getAccessibleDocuments(User user, CrossSystemRequest request) {
        // 根据用户项目权限获取可访问的文档
        List<String> accessibleProjects = getAccessibleProjects(user, request);
        return getDocumentsByProjects(accessibleProjects);
    }

    private List<String> mapProjectsToCategories(List<String> projectIds) {
        // 实现项目到分类的映射逻辑
        // 这里可以通过配置文件或数据库查询实现
        return projectIds.stream()
            .map(this::getCategoryByProject)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    private String getCategoryByProject(String projectId) {
        // 根据项目ID获取对应的分类ID
        // 这里需要根据具体的业务映射关系实现
        return "CAT_" + projectId;
    }

    private List<String> getDocumentsByProjects(List<String> projectIds) {
        // 根据项目ID列表获取相关文档
        return projectIds.stream()
            .flatMap(projectId -> getDocumentsByProject(projectId).stream())
            .distinct()
            .collect(Collectors.toList());
    }

    private List<String> getDocumentsByProject(String projectId) {
        // 根据项目ID获取文档列表
        // 这里可以调用相关服务获取
        return Arrays.asList("DOC_" + projectId + "_001", "DOC_" + projectId + "_002");
    }
}
```

#### 4.1.3 跨系统调用控制器

```java
@RestController
@RequestMapping("/api/cross-system")
@Slf4j
public class CrossSystemController {

    @Autowired
    private CrossSystemAuthService authService;

    @Autowired
    private UserService userService;

    /**
     * 唤起JJWD平台
     */
    @PostMapping("/launch-jjwd")
    public ResponseEntity<CrossSystemResponse> launchJJWD(
            @RequestBody CrossSystemRequest request,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            log.info("用户[{}]请求唤起JJWD平台，目标页面: {}", username, request.getTargetPage());

            // 权限预检查
            validateUserPermission(username, request);

            // 生成跨系统Token
            CrossSystemToken token = authService.generateCrossSystemToken(username, request);

            CrossSystemResponse response = CrossSystemResponse.builder()
                .success(true)
                .redirectUrl(token.getRedirectUrl())
                .token(token.getToken())
                .expiresIn(token.getExpiresIn())
                .message("跳转链接生成成功")
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("唤起JJWD平台失败", e);
            CrossSystemResponse response = CrossSystemResponse.builder()
                .success(false)
                .message("唤起失败: " + e.getMessage())
                .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void validateUserPermission(String username, CrossSystemRequest request) {
        // 验证用户是否有权限访问指定的分类/项目/文档
        User user = userService.findByUsername(username);

        // 检查分类权限
        if (StringUtils.hasText(request.getCategoryId())) {
            if (!hasAccessToCategory(user, request.getCategoryId())) {
                throw new AccessDeniedException("用户无权限访问分类: " + request.getCategoryId());
            }
        }

        // 检查项目权限
        if (StringUtils.hasText(request.getProjectId())) {
            if (!hasAccessToProject(user, request.getProjectId())) {
                throw new AccessDeniedException("用户无权限访问项目: " + request.getProjectId());
            }
        }

        // 检查文档权限
        if (StringUtils.hasText(request.getDocumentId())) {
            if (!hasAccessToDocument(user, request.getDocumentId())) {
                throw new AccessDeniedException("用户无权限访问文档: " + request.getDocumentId());
            }
        }
    }

    private boolean hasAccessToCategory(User user, String categoryId) {
        // 实现分类权限检查逻辑
        return user.getProjects().stream()
            .anyMatch(project -> ("CAT_" + project.getId()).equals(categoryId));
    }

    private boolean hasAccessToProject(User user, String projectId) {
        // 实现项目权限检查逻辑
        return user.getProjects().stream()
            .anyMatch(project -> project.getId().equals(projectId));
    }

    private boolean hasAccessToDocument(User user, String documentId) {
        // 实现文档权限检查逻辑
        return user.getProjects().stream()
            .anyMatch(project ->
                documentId.startsWith("DOC_" + project.getId()));
    }
}

// 数据传输对象
@Data
@Builder
public class CrossSystemRequest {
    private String targetPage;
    private String categoryId;
    private String projectId;
    private String documentId;
    private String action;
    private Map<String, Object> extraParams;
}

@Data
@Builder
public class CrossSystemResponse {
    private boolean success;
    private String redirectUrl;
    private String token;
    private Integer expiresIn;
    private String message;
}

@Data
@Builder
public class CrossSystemToken {
    private String token;
    private String redirectUrl;
    private Integer expiresIn;
}
```

### 4.2 JJWD系统后端实现

#### 4.2.1 跨系统Token验证服务

```java
@Service
@Slf4j
public class CrossSystemTokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${cross-system.pms.app-key}")
    private String pmsAppKey;

    @Autowired
    private UserSyncService userSyncService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 验证并解析跨系统Token
     */
    public CrossSystemUserInfo validateAndParseToken(String token) {
        try {
            // 解析JWT Token
            Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

            // 验证Token基本信息
            validateTokenClaims(claims);

            // 验证APP Key
            String appKey = (String) claims.get("app_key");
            if (!pmsAppKey.equals(appKey)) {
                throw new InvalidTokenException("APP Key验证失败");
            }

            // 检查Token是否已使用（防重放攻击）
            String tokenId = claims.getId();
            if (isTokenUsed(tokenId)) {
                throw new InvalidTokenException("Token已被使用");
            }

            // 标记Token为已使用
            markTokenAsUsed(tokenId, getTokenExpireSeconds(claims));

            // 提取用户信息
            return extractUserInfo(claims);

        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token已过期");
        } catch (JwtException e) {
            throw new InvalidTokenException("Token验证失败: " + e.getMessage());
        }
    }

    private void validateTokenClaims(Claims claims) {
        // 验证发行者
        if (!"PMS-System".equals(claims.getIssuer())) {
            throw new InvalidTokenException("Token发行者不匹配");
        }

        // 验证接收者
        if (!"JJWD-Platform".equals(claims.getAudience())) {
            throw new InvalidTokenException("Token接收者不匹配");
        }

        // 验证过期时间
        if (claims.getExpiration().before(new Date())) {
            throw new InvalidTokenException("Token已过期");
        }
    }

    private boolean isTokenUsed(String tokenId) {
        String key = "cross_system_token:" + tokenId;
        return redisTemplate.hasKey(key);
    }

    private void markTokenAsUsed(String tokenId, int expireSeconds) {
        String key = "cross_system_token:" + tokenId;
        redisTemplate.opsForValue().set(key, "used", expireSeconds, TimeUnit.SECONDS);
    }

    private int getTokenExpireSeconds(Claims claims) {
        long expireTime = claims.getExpiration().getTime();
        long currentTime = System.currentTimeMillis();
        return (int) ((expireTime - currentTime) / 1000);
    }

    private CrossSystemUserInfo extractUserInfo(Claims claims) {
        Map<String, Object> userInfo = (Map<String, Object>) claims.get("user_info");
        Map<String, Object> permissions = (Map<String, Object>) claims.get("permissions");
        Map<String, Object> context = (Map<String, Object>) claims.get("context");

        return CrossSystemUserInfo.builder()
            .username((String) userInfo.get("username"))
            .email((String) userInfo.get("email"))
            .displayName((String) userInfo.get("display_name"))
            .department((String) userInfo.get("department"))
            .employeeId((String) userInfo.get("employee_id"))
            .ldapGroups((List<String>) userInfo.get("ldap_groups"))
            .roles((List<String>) permissions.get("roles"))
            .actions((List<String>) permissions.get("actions"))
            .categories((List<String>) permissions.get("categories"))
            .projects((List<String>) permissions.get("projects"))
            .documents((List<String>) permissions.get("documents"))
            .targetPage((String) context.get("target_page"))
            .categoryId((String) context.get("category_id"))
            .projectId((String) context.get("project_id"))
            .documentId((String) context.get("document_id"))
            .action((String) context.get("action"))
            .build();
    }
}

@Data
@Builder
public class CrossSystemUserInfo {
    // 用户基本信息
    private String username;
    private String email;
    private String displayName;
    private String department;
    private String employeeId;
    private List<String> ldapGroups;

    // 权限信息
    private List<String> roles;
    private List<String> actions;
    private List<String> categories;
    private List<String> projects;
    private List<String> documents;

    // 上下文信息
    private String targetPage;
    private String categoryId;
    private String projectId;
    private String documentId;
    private String action;
}
```

#### 4.2.2 用户同步服务

```java
@Service
@Transactional
public class UserSyncService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 同步跨系统用户信息
     */
    public User syncCrossSystemUser(CrossSystemUserInfo userInfo) {
        // 查找或创建本地用户
        User localUser = userRepository.findByUsername(userInfo.getUsername())
            .orElseGet(() -> createNewUser(userInfo));

        // 同步用户基本信息
        updateUserBasicInfo(localUser, userInfo);

        // 同步用户角色
        syncUserRoles(localUser, userInfo.getRoles());

        // 同步数据权限
        syncDataPermissions(localUser, userInfo);

        return userRepository.save(localUser);
    }

    private User createNewUser(CrossSystemUserInfo userInfo) {
        User user = new User();
        user.setUsername(userInfo.getUsername());
        user.setAuthType("CROSS_SYSTEM");
        user.setEnabled(true);
        user.setCreatedAt(new Date());
        return user;
    }

    private void updateUserBasicInfo(User user, CrossSystemUserInfo userInfo) {
        user.setEmail(userInfo.getEmail());
        user.setDisplayName(userInfo.getDisplayName());
        user.setDepartment(userInfo.getDepartment());
        user.setEmployeeId(userInfo.getEmployeeId());
        user.setLdapGroups(userInfo.getLdapGroups());
        user.setLastLoginAt(new Date());
    }

    private void syncUserRoles(User user, List<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> createRoleIfNotExists(roleName));
            roles.add(role);
        }
        user.setRoles(roles);
    }

    private Role createRoleIfNotExists(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        role.setDescription("从PMS系统同步的角色: " + roleName);
        role.setCreatedAt(new Date());
        return roleRepository.save(role);
    }

    private void syncDataPermissions(User user, CrossSystemUserInfo userInfo) {
        // 同步分类权限
        syncCategoryPermissions(user, userInfo.getCategories());

        // 同步项目权限
        syncProjectPermissions(user, userInfo.getProjects());

        // 同步文档权限
        syncDocumentPermissions(user, userInfo.getDocuments());
    }

    private void syncCategoryPermissions(User user, List<String> categoryIds) {
        Set<Category> categories = new HashSet<>();
        for (String categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                .orElseGet(() -> createCategoryIfNotExists(categoryId));
            categories.add(category);
        }
        user.setAccessibleCategories(categories);
    }

    private void syncProjectPermissions(User user, List<String> projectIds) {
        Set<Project> projects = new HashSet<>();
        for (String projectId : projectIds) {
            Project project = projectRepository.findById(projectId)
                .orElseGet(() -> createProjectIfNotExists(projectId));
            projects.add(project);
        }
        user.setAccessibleProjects(projects);
    }

    private void syncDocumentPermissions(User user, List<String> documentIds) {
        // 这里可以根据需要实现文档权限同步
        // 通常文档权限通过分类和项目权限继承
    }
}
```

#### 4.2.3 跨系统认证控制器

```java
@Controller
@RequestMapping("/auth")
@Slf4j
public class CrossSystemAuthController {

    @Autowired
    private CrossSystemTokenService tokenService;

    @Autowired
    private UserSyncService userSyncService;

    /**
     * 跨系统登录入口
     */
    @GetMapping("/cross-system-login")
    public String crossSystemLogin(
            @RequestParam String token,
            @RequestParam(required = false) String redirect,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            log.info("收到跨系统登录请求，Token: {}, 重定向: {}",
                token.substring(0, Math.min(20, token.length())) + "...", redirect);

            // 验证并解析Token
            CrossSystemUserInfo userInfo = tokenService.validateAndParseToken(token);

            // 同步用户到本地系统
            User localUser = userSyncService.syncCrossSystemUser(userInfo);

            // 创建本地认证会话
            createLocalAuthSession(localUser, userInfo, request);

            // 重定向到目标页面
            String targetUrl = determineTargetUrl(userInfo, redirect);

            log.info("跨系统登录成功，用户: {}, 重定向到: {}",
                localUser.getUsername(), targetUrl);

            return "redirect:" + targetUrl;

        } catch (InvalidTokenException e) {
            log.error("跨系统登录失败，Token验证错误: {}", e.getMessage());
            return "redirect:/login?error=invalid_cross_system_token";
        } catch (Exception e) {
            log.error("跨系统登录失败", e);
            return "redirect:/login?error=cross_system_login_failed";
        }
    }

    private void createLocalAuthSession(User user, CrossSystemUserInfo userInfo, HttpServletRequest request) {
        // 创建Spring Security认证对象
        List<GrantedAuthority> authorities = userInfo.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());

        // 添加操作权限
        userInfo.getActions().forEach(action ->
            authorities.add(new SimpleGrantedAuthority("PERM_" + action)));

        // 创建自定义UserDetails
        CrossSystemUserDetails userDetails = new CrossSystemUserDetails(
            user.getUsername(),
            "N/A", // 跨系统用户无密码
            authorities,
            userInfo
        );

        // 创建认证Token
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        // 设置认证详情
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 设置到Security Context
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 保存到Session
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext());

        // 设置跨系统用户标识
        session.setAttribute("CROSS_SYSTEM_USER", true);
        session.setAttribute("CROSS_SYSTEM_SOURCE", "PMS");
        session.setAttribute("CROSS_SYSTEM_USER_INFO", userInfo);
    }

    private String determineTargetUrl(CrossSystemUserInfo userInfo, String redirect) {
        // 优先使用redirect参数
        if (StringUtils.hasText(redirect)) {
            return redirect;
        }

        // 其次使用Token中的目标页面
        if (StringUtils.hasText(userInfo.getTargetPage())) {
            return userInfo.getTargetPage();
        }

        // 最后使用默认首页
        return "/dashboard";
    }
}

/**
 * 跨系统用户详情类
 */
public class CrossSystemUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final CrossSystemUserInfo crossSystemUserInfo;

    public CrossSystemUserDetails(String username, String password,
                                 Collection<? extends GrantedAuthority> authorities,
                                 CrossSystemUserInfo crossSystemUserInfo) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.crossSystemUserInfo = crossSystemUserInfo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public CrossSystemUserInfo getCrossSystemUserInfo() {
        return crossSystemUserInfo;
    }
}
```

#### 4.2.4 权限控制拦截器

```java
@Component
public class CrossSystemPermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) throws Exception {

        // 检查是否为跨系统用户
        HttpSession session = request.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("CROSS_SYSTEM_USER"))) {
            return true; // 非跨系统用户，使用正常权限控制
        }

        // 获取跨系统用户信息
        CrossSystemUserInfo userInfo = (CrossSystemUserInfo) session.getAttribute("CROSS_SYSTEM_USER_INFO");
        if (userInfo == null) {
            response.sendRedirect("/login?error=cross_system_session_expired");
            return false;
        }

        // 检查资源访问权限
        String requestURI = request.getRequestURI();
        if (!hasAccessToResource(userInfo, requestURI, request)) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "跨系统用户无权限访问此资源");
            return false;
        }

        return true;
    }

    private boolean hasAccessToResource(CrossSystemUserInfo userInfo, String requestURI,
                                      HttpServletRequest request) {

        // 检查分类访问权限
        if (requestURI.contains("/category/")) {
            String categoryId = extractCategoryId(requestURI, request);
            if (categoryId != null && !userInfo.getCategories().contains(categoryId)) {
                return false;
            }
        }

        // 检查项目访问权限
        if (requestURI.contains("/project/")) {
            String projectId = extractProjectId(requestURI, request);
            if (projectId != null && !userInfo.getProjects().contains(projectId)) {
                return false;
            }
        }

        // 检查文档访问权限
        if (requestURI.contains("/document/")) {
            String documentId = extractDocumentId(requestURI, request);
            if (documentId != null && !userInfo.getDocuments().contains(documentId)) {
                return false;
            }
        }

        // 检查操作权限
        String httpMethod = request.getMethod();
        String requiredAction = mapHttpMethodToAction(httpMethod, requestURI);
        if (requiredAction != null && !userInfo.getActions().contains(requiredAction)) {
            return false;
        }

        return true;
    }

    private String extractCategoryId(String requestURI, HttpServletRequest request) {
        // 从URI或请求参数中提取分类ID
        String categoryId = request.getParameter("categoryId");
        if (categoryId != null) {
            return categoryId;
        }

        // 从URI路径中提取
        Pattern pattern = Pattern.compile("/category/([^/]+)");
        Matcher matcher = pattern.matcher(requestURI);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String extractProjectId(String requestURI, HttpServletRequest request) {
        // 类似extractCategoryId的实现
        String projectId = request.getParameter("projectId");
        if (projectId != null) {
            return projectId;
        }

        Pattern pattern = Pattern.compile("/project/([^/]+)");
        Matcher matcher = pattern.matcher(requestURI);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String extractDocumentId(String requestURI, HttpServletRequest request) {
        // 类似extractCategoryId的实现
        String documentId = request.getParameter("documentId");
        if (documentId != null) {
            return documentId;
        }

        Pattern pattern = Pattern.compile("/document/([^/]+)");
        Matcher matcher = pattern.matcher(requestURI);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String mapHttpMethodToAction(String httpMethod, String requestURI) {
        // 根据HTTP方法和URI映射到操作权限
        switch (httpMethod.toUpperCase()) {
            case "GET":
                return "READ";
            case "POST":
                if (requestURI.contains("/create")) {
                    return "CREATE";
                }
                return "EDIT";
            case "PUT":
            case "PATCH":
                return "EDIT";
            case "DELETE":
                return "DELETE";
            default:
                return null;
        }
    }
}
```

## 5. 前端实现方案

### 5.1 PMS系统前端实现

#### 5.1.1 跨系统唤起服务

```javascript
// PMS系统 - 跨系统唤起服务
class CrossSystemLauncher {
    constructor() {
        this.baseURL = '/api/cross-system';
    }

    /**
     * 唤起JJWD平台
     */
    async launchJJWD(options) {
        const {
            targetPage,
            categoryId,
            projectId,
            documentId,
            action = 'READ',
            extraParams = {}
        } = options;

        try {
            const response = await this.request('/launch-jjwd', {
                targetPage,
                categoryId,
                projectId,
                documentId,
                action,
                extraParams
            });

            if (response.success) {
                // 在新窗口打开JJWD平台
                this.openJJWDWindow(response.redirectUrl);
                return { success: true, message: '成功唤起文档平台' };
            } else {
                throw new Error(response.message || '唤起失败');
            }

        } catch (error) {
            console.error('唤起JJWD平台失败:', error);
            return { success: false, message: error.message };
        }
    }

    /**
     * 发送HTTP请求
     */
    async request(endpoint, data) {
        const response = await fetch(this.baseURL + endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.getAuthToken()}`
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    }

    /**
     * 获取当前用户的认证Token
     */
    getAuthToken() {
        return localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    }

    /**
     * 在新窗口打开JJWD平台
     */
    openJJWDWindow(url) {
        const windowFeatures = 'width=1200,height=800,scrollbars=yes,resizable=yes';
        const newWindow = window.open(url, 'jjwd_platform', windowFeatures);

        if (!newWindow) {
            throw new Error('无法打开新窗口，请检查浏览器弹窗设置');
        }

        // 监听窗口关闭事件
        const checkClosed = setInterval(() => {
            if (newWindow.closed) {
                clearInterval(checkClosed);
                console.log('JJWD平台窗口已关闭');
            }
        }, 1000);
    }
}

// 全局实例
const crossSystemLauncher = new CrossSystemLauncher();
```

#### 5.1.2 业务组件集成

```vue
<!-- PMS系统 - 项目详情页面 -->
<template>
  <div class="project-detail">
    <div class="project-header">
      <h2>{{ project.name }}</h2>
      <div class="action-buttons">
        <el-button
          type="primary"
          @click="openDocumentEditor"
          :loading="launching">
          编辑项目文档
        </el-button>
        <el-button
          @click="openDocumentViewer"
          :loading="launching">
          查看项目文档
        </el-button>
      </div>
    </div>

    <div class="project-content">
      <!-- 项目内容 -->
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  project: {
    type: Object,
    required: true
  }
})

const launching = ref(false)

/**
 * 打开文档编辑器
 */
async function openDocumentEditor() {
  launching.value = true

  try {
    const result = await crossSystemLauncher.launchJJWD({
      targetPage: `/document/edit/${props.project.documentId}`,
      categoryId: props.project.categoryId,
      projectId: props.project.id,
      documentId: props.project.documentId,
      action: 'EDIT'
    })

    if (result.success) {
      ElMessage.success('文档编辑器已打开')
    } else {
      ElMessage.error(result.message)
    }

  } catch (error) {
    ElMessage.error('打开文档编辑器失败')
  } finally {
    launching.value = false
  }
}

/**
 * 打开文档查看器
 */
async function openDocumentViewer() {
  launching.value = true

  try {
    const result = await crossSystemLauncher.launchJJWD({
      targetPage: `/document/view/${props.project.documentId}`,
      categoryId: props.project.categoryId,
      projectId: props.project.id,
      documentId: props.project.documentId,
      action: 'READ'
    })

    if (result.success) {
      ElMessage.success('文档查看器已打开')
    } else {
      ElMessage.error(result.message)
    }

  } catch (error) {
    ElMessage.error('打开文档查看器失败')
  } finally {
    launching.value = false
  }
}
</script>
```

### 5.2 JJWD系统前端实现

#### 5.2.1 跨系统认证处理

```javascript
// JJWD系统 - 跨系统认证处理
class CrossSystemAuthHandler {
    constructor() {
        this.init();
    }

    init() {
        // 检查URL参数中是否有跨系统认证Token
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');

        if (token) {
            this.handleCrossSystemAuth(token);
        }
    }

    /**
     * 处理跨系统认证
     */
    async handleCrossSystemAuth(token) {
        try {
            // 显示登录进度
            this.showLoginProgress();

            // 自动提交跨系统登录表单
            this.submitCrossSystemLogin(token);

        } catch (error) {
            console.error('跨系统认证失败:', error);
            this.showError('跨系统登录失败，请重试');
        }
    }

    /**
     * 提交跨系统登录表单
     */
    submitCrossSystemLogin(token) {
        const urlParams = new URLSearchParams(window.location.search);
        const redirect = urlParams.get('redirect');

        // 创建隐藏表单
        const form = document.createElement('form');
        form.method = 'GET';
        form.action = '/auth/cross-system-login';

        // 添加token参数
        const tokenInput = document.createElement('input');
        tokenInput.type = 'hidden';
        tokenInput.name = 'token';
        tokenInput.value = token;
        form.appendChild(tokenInput);

        // 添加redirect参数
        if (redirect) {
            const redirectInput = document.createElement('input');
            redirectInput.type = 'hidden';
            redirectInput.name = 'redirect';
            redirectInput.value = redirect;
            form.appendChild(redirectInput);
        }

        // 提交表单
        document.body.appendChild(form);
        form.submit();
    }

    /**
     * 显示登录进度
     */
    showLoginProgress() {
        const overlay = document.createElement('div');
        overlay.id = 'cross-system-login-overlay';
        overlay.innerHTML = `
            <div class="login-overlay">
                <div class="login-progress">
                    <div class="spinner"></div>
                    <div class="message">正在从PMS系统登录...</div>
                    <div class="progress-bar">
                        <div class="progress-fill"></div>
                    </div>
                </div>
            </div>
            <style>
                .login-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0, 0, 0, 0.7);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 9999;
                }
                .login-progress {
                    background: white;
                    padding: 30px;
                    border-radius: 8px;
                    text-align: center;
                    min-width: 300px;
                }
                .spinner {
                    width: 40px;
                    height: 40px;
                    border: 4px solid #f3f3f3;
                    border-top: 4px solid #1890ff;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                    margin: 0 auto 20px;
                }
                .message {
                    font-size: 16px;
                    color: #333;
                    margin-bottom: 20px;
                }
                .progress-bar {
                    width: 100%;
                    height: 4px;
                    background: #f0f0f0;
                    border-radius: 2px;
                    overflow: hidden;
                }
                .progress-fill {
                    height: 100%;
                    background: #1890ff;
                    border-radius: 2px;
                    animation: progress 3s ease-in-out infinite;
                }
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
                @keyframes progress {
                    0% { width: 0%; }
                    50% { width: 70%; }
                    100% { width: 100%; }
                }
            </style>
        `;

        document.body.appendChild(overlay);
    }

    /**
     * 显示错误信息
     */
    showError(message) {
        // 移除登录进度
        const overlay = document.getElementById('cross-system-login-overlay');
        if (overlay) {
            overlay.remove();
        }

        // 显示错误信息
        alert(message);

        // 重定向到登录页面
        window.location.href = '/login?error=cross_system_auth_failed';
    }
}

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', () => {
    new CrossSystemAuthHandler();
});
```

#### 5.2.2 HTTP拦截器改造

```javascript
// JJWD系统 - HTTP拦截器改造
import axios from 'axios'

// 创建axios实例
const httpClient = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器
httpClient.interceptors.request.use(
    config => {
        // 检查是否为跨系统用户
        const isCrossSystemUser = sessionStorage.getItem('CROSS_SYSTEM_USER') === 'true'

        if (isCrossSystemUser) {
            // 跨系统用户不需要添加本地token
            config.headers['X-Cross-System-User'] = 'true'
        } else {
            // 本地用户添加token
            const token = localStorage.getItem('auth_token')
            if (token) {
                config.headers['Authorization'] = `Bearer ${token}`
            }
        }

        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
httpClient.interceptors.response.use(
    response => {
        return response
    },
    error => {
        const { response } = error

        if (response && response.status === 401) {
            // 检查是否为跨系统用户
            const isCrossSystemUser = sessionStorage.getItem('CROSS_SYSTEM_USER') === 'true'

            if (isCrossSystemUser) {
                // 跨系统用户认证失效，重定向到PMS系统
                window.location.href = '/login?error=cross_system_session_expired'
            } else {
                // 本地用户认证失效，重定向到本地登录页
                localStorage.removeItem('auth_token')
                window.location.href = '/login'
            }
        }

        return Promise.reject(error)
    }
)

export default httpClient
```

#### 5.2.3 路由守卫改造

```javascript
// JJWD系统 - 路由守卫改造
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        // 路由配置
    ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
    // 检查是否需要认证
    if (to.meta.requiresAuth) {
        const isAuthenticated = checkAuthentication()

        if (!isAuthenticated) {
            // 未认证，重定向到登录页
            next('/login')
            return
        }

        // 检查权限
        if (to.meta.requiredPermissions) {
            const hasPermission = checkPermissions(to.meta.requiredPermissions)

            if (!hasPermission) {
                // 无权限，显示403页面
                next('/403')
                return
            }
        }
    }

    next()
})

/**
 * 检查用户认证状态
 */
function checkAuthentication() {
    // 检查是否为跨系统用户
    const isCrossSystemUser = sessionStorage.getItem('CROSS_SYSTEM_USER') === 'true'

    if (isCrossSystemUser) {
        // 跨系统用户检查session
        return checkCrossSystemSession()
    } else {
        // 本地用户检查token
        return checkLocalToken()
    }
}

/**
 * 检查跨系统用户session
 */
function checkCrossSystemSession() {
    // 这里可以通过AJAX请求检查session是否有效
    // 简单起见，这里只检查sessionStorage
    const crossSystemUserInfo = sessionStorage.getItem('CROSS_SYSTEM_USER_INFO')
    return crossSystemUserInfo !== null
}

/**
 * 检查本地用户token
 */
function checkLocalToken() {
    const token = localStorage.getItem('auth_token')
    if (!token) {
        return false
    }

    // 这里可以添加token有效性检查
    try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        return payload.exp > Date.now() / 1000
    } catch (error) {
        return false
    }
}

/**
 * 检查用户权限
 */
function checkPermissions(requiredPermissions) {
    const isCrossSystemUser = sessionStorage.getItem('CROSS_SYSTEM_USER') === 'true'

    if (isCrossSystemUser) {
        return checkCrossSystemPermissions(requiredPermissions)
    } else {
        return checkLocalPermissions(requiredPermissions)
    }
}

/**
 * 检查跨系统用户权限
 */
function checkCrossSystemPermissions(requiredPermissions) {
    const userInfoStr = sessionStorage.getItem('CROSS_SYSTEM_USER_INFO')
    if (!userInfoStr) {
        return false
    }

    try {
        const userInfo = JSON.parse(userInfoStr)
        const userPermissions = [...(userInfo.roles || []), ...(userInfo.actions || [])]

        return requiredPermissions.every(permission =>
            userPermissions.includes(permission))
    } catch (error) {
        return false
    }
}

/**
 * 检查本地用户权限
 */
function checkLocalPermissions(requiredPermissions) {
    // 从localStorage或其他地方获取本地用户权限
    const userPermissions = JSON.parse(localStorage.getItem('user_permissions') || '[]')

    return requiredPermissions.every(permission =>
        userPermissions.includes(permission))
}

export default router
```

## 6. 配置文件

### 6.1 PMS系统配置

```yaml
# application.yml
spring:
  application:
    name: pms-system

  # JWT配置
jwt:
  secret: ${JWT_SECRET:your-super-secret-jwt-key-for-cross-system-auth}
  expiration: 3600

# 跨系统配置
cross-system:
  jjwd:
    app-key: ${JJWD_APP_KEY:pms_to_jjwd_app_key_12345}
    base-url: ${JJWD_BASE_URL:http://localhost:8081}
    enabled: true

# 权限映射配置
permission-mapping:
  role-mapping:
    "PROJECT_MANAGER": "DOCUMENT_ADMIN"
    "DEVELOPER": "DOCUMENT_EDITOR"
    "TESTER": "DOCUMENT_VIEWER"
    "ADMIN": "SYSTEM_ADMIN"

  action-mapping:
    "PROJECT_READ": "DOCUMENT_READ"
    "PROJECT_EDIT": "DOCUMENT_EDIT"
    "PROJECT_CREATE": "DOCUMENT_CREATE"
    "PROJECT_DELETE": "DOCUMENT_DELETE"

# 日志配置
logging:
  level:
    com.company.pms.crosssystem: DEBUG
```

### 6.2 JJWD系统配置

```yaml
# application.yml
spring:
  application:
    name: jjwd-platform

  # Redis配置（用于Token防重放）
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0

# JWT配置（必须与PMS系统相同）
jwt:
  secret: ${JWT_SECRET:your-super-secret-jwt-key-for-cross-system-auth}

# 跨系统配置
cross-system:
  pms:
    app-key: ${PMS_APP_KEY:pms_to_jjwd_app_key_12345}
    base-url: ${PMS_BASE_URL:http://localhost:8080}
    trusted: true

  # 权限映射配置
  permission-mapping:
    enabled: true
    strict-mode: true # 严格模式，只允许映射的权限

# 安全配置
security:
  cross-system:
    token-max-age: 3600 # Token最大有效期（秒）
    session-timeout: 7200 # 跨系统用户session超时时间（秒）

# 日志配置
logging:
  level:
    com.company.jjwd.crosssystem: DEBUG
```

## 7. 部署和运维

### 7.1 环境变量配置

```bash
# 环境变量配置文件 .env

# JWT密钥（两个系统必须相同）
JWT_SECRET=your-super-secret-jwt-key-for-cross-system-auth-min-256-bits

# APP Key（用于系统间认证）
PMS_APP_KEY=pms_to_jjwd_app_key_12345
JJWD_APP_KEY=pms_to_jjwd_app_key_12345

# 系统URL配置
PMS_BASE_URL=https://pms.company.com
JJWD_BASE_URL=https://jjwd.company.com

# Redis配置
REDIS_HOST=redis.company.com
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# 数据库配置
DB_HOST=db.company.com
DB_PORT=3306
DB_NAME=jjwd_platform
DB_USERNAME=jjwd_user
DB_PASSWORD=your-db-password
```

### 7.2 安全检查清单

1. **密钥安全**
   - [ ] JWT密钥长度至少256位
   - [ ] 定期轮换JWT密钥
   - [ ] APP Key使用强随机字符串
   - [ ] 所有密钥存储在环境变量中

2. **网络安全**
   - [ ] 系统间通信使用HTTPS
   - [ ] 配置防火墙规则
   - [ ] 使用内网或VPN隔离

3. **权限控制**
   - [ ] 实现最小权限原则
   - [ ] 定期审计权限映射配置
   - [ ] 监控异常权限访问

4. **监控告警**
   - [ ] Token验证失败告警
   - [ ] 异常登录行为监控
   - [ ] 系统间调用失败告警

## 8. 总结

本技术方案通过JWT Token + APP Key的混合认证模式，成功解决了PMS系统与JJWD平台的集成问题：

### 8.1 解决的核心问题

1. **身份认证同步**：通过JWT Token传递完整用户信息，实现跨系统身份同步
2. **权限映射**：配置化的权限映射机制，确保权限正确传递
3. **数据层级控制**：支持分类、项目、文档的细粒度权限控制
4. **前端状态管理**：通过特殊的认证处理机制，解决Vue3无状态管理的问题

### 8.2 方案优势

1. **安全性高**：JWT签名验证、Token防重放、权限细粒度控制
2. **扩展性好**：支持更多系统接入，权限映射配置灵活
3. **用户体验佳**：无缝跳转，单点登录效果
4. **维护性强**：代码结构清晰，配置化管理

### 8.3 实施建议

1. **分阶段实施**：先实现基础认证，再完善权限控制
2. **充分测试**：重点测试跨系统认证和权限控制
3. **监控完善**：建立完善的监控和告警机制
4. **文档维护**：保持技术文档和配置文档的更新

该方案为企业内部系统集成提供了一个安全、可靠、可扩展的解决方案。
```
```

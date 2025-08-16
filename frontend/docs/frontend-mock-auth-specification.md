# 前端Mock数据与权限认证规范

## 一、用户角色定义

### 1.1 系统角色
1. 系统管理员（ADMIN）
   - 最高权限级别
   - 可以管理所有功能和数据
   - 可以配置系统参数
   - 可以管理其他用户权限

2. 项目经理（PROJECT_MANAGER）
   - 可以创建和管理项目
   - 可以分配团队成员
   - 可以审核文档
   - 可以查看统计报表

3. 投标专员（BID_SPECIALIST）
   - 可以编辑标书文档
   - 可以查看项目信息
   - 可以提交审核申请
   - 可以协作编辑文档

4. 技术专家（TECHNICAL_EXPERT）
   - 可以编辑技术方案
   - 可以审核技术内容
   - 可以提供技术建议
   - 可以查看技术文档

5. 普通用户（NORMAL_USER）
   - 可以查看已分配项目
   - 可以编辑已分配文档
   - 可以提交文档审核
   - 可以查看基本信息

### 1.2 Mock用户数据
```typescript
interface MockUser {
  id: string;
  username: string;
  name: string;
  role: string;
  department: string;
  email: string;
  phone: string;
  avatar: string;
  permissions: string[];
}

const mockUsers: MockUser[] = [
  {
    id: "admin001",
    username: "admin",
    name: "系统管理员",
    role: "ADMIN",
    department: "系统管理部",
    email: "admin@example.com",
    phone: "13800000001",
    avatar: "/avatars/admin.png",
    permissions: ["*"]
  },
  {
    id: "pm001",
    username: "projectmanager",
    name: "张项目",
    role: "PROJECT_MANAGER",
    department: "项目管理部",
    email: "pm@example.com",
    phone: "13800000002",
    avatar: "/avatars/pm.png",
    permissions: [
      "project:create",
      "project:edit",
      "project:delete",
      "project:view",
      "team:manage",
      "document:review"
    ]
  },
  {
    id: "bid001",
    username: "bidspecialist",
    name: "李投标",
    role: "BID_SPECIALIST",
    department: "投标部",
    email: "bid@example.com",
    phone: "13800000003",
    avatar: "/avatars/bid.png",
    permissions: [
      "project:view",
      "document:edit",
      "document:submit",
      "document:view"
    ]
  }
];
```

## 二、权限控制实现

### 2.1 权限数据结构
```typescript
interface Permission {
  code: string;
  name: string;
  description: string;
  dependencies?: string[];
}

const permissions: Permission[] = [
  {
    code: "project:create",
    name: "创建项目",
    description: "允许创建新的投标项目"
  },
  {
    code: "project:edit",
    name: "编辑项目",
    description: "允许编辑项目基本信息",
    dependencies: ["project:view"]
  },
  {
    code: "project:delete",
    name: "删除项目",
    description: "允许删除项目",
    dependencies: ["project:edit"]
  }
];
```

### 2.2 权限检查HOC
```typescript
import React from 'react';
import { useAuth } from '@/hooks/useAuth';

interface WithPermissionProps {
  permission: string | string[];
  fallback?: React.ReactNode;
}

export const withPermission = <P extends object>(
  WrappedComponent: React.ComponentType<P>,
  { permission, fallback = null }: WithPermissionProps
) => {
  return (props: P) => {
    const { hasPermission } = useAuth();
    
    if (!hasPermission(permission)) {
      return fallback;
    }
    
    return <WrappedComponent {...props} />;
  };
};
```

### 2.3 权限Hook
```typescript
import { useContext } from 'react';
import { AuthContext } from '@/contexts/AuthContext';

export const useAuth = () => {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  
  return {
    ...context,
    hasPermission: (permission: string | string[]) => {
      if (Array.isArray(permission)) {
        return permission.every(p => context.permissions.includes(p));
      }
      return context.permissions.includes(permission);
    }
  };
};
```

## 三、登录认证流程

### 3.1 登录流程
1. 用户输入用户名和密码
2. 前端进行基本验证
3. 调用登录API
4. 获取token和用户信息
5. 存储到localStorage
6. 更新全局状态
7. 跳转到首页

```typescript
interface LoginParams {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  user: MockUser;
}

const login = async (params: LoginParams): Promise<LoginResponse> => {
  // Mock API调用
  const response = await mockLoginApi(params);
  
  // 存储token
  localStorage.setItem('token', response.token);
  
  // 存储用户信息
  localStorage.setItem('user', JSON.stringify(response.user));
  
  return response;
};
```

### 3.2 登录状态维护
```typescript
interface AuthState {
  isAuthenticated: boolean;
  user: MockUser | null;
  token: string | null;
}

const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  token: null
};

const AuthProvider: React.FC = ({ children }) => {
  const [state, setState] = useState(initialState);
  
  useEffect(() => {
    // 初始化时检查localStorage
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    if (token && user) {
      setState({
        isAuthenticated: true,
        token,
        user: JSON.parse(user)
      });
    }
  }, []);
  
  return (
    <AuthContext.Provider value={{ ...state, setState }}>
      {children}
    </AuthContext.Provider>
  );
};
```

## 四、Mock API实现

### 4.1 Mock Service配置
```typescript
import Mock from 'mockjs';

// 配置Mock延迟
Mock.setup({
  timeout: '200-600'
});

// 配置请求前缀
const API_PREFIX = '/api/v1';
```

### 4.2 项目列表接口
```typescript
interface ProjectListParams {
  page: number;
  pageSize: number;
  status?: string;
  type?: string;
  keyword?: string;
  dateRange?: [string, string];
  department?: string;
}

Mock.mock(new RegExp(`${API_PREFIX}/projects`), 'get', (options: any) => {
  const params = parseQueryString(options.url);
  
  return Mock.mock({
    'code': 200,
    'message': 'success',
    'data': {
      'total': '@integer(100, 200)',
      [`list|${params.pageSize}`]: [{
        'id': '@id',
        'name': '@ctitle(10, 20)',
        'type|1': ['产品类', '服务类', '工程类'],
        'status|1': ['进行中', '已完成', '已废标'],
        'customer': '@cname',
        'amount': '@float(10000, 1000000, 2, 2)',
        'createTime': '@datetime',
        'deadline': '@datetime',
        'manager': '@cname',
        'department': '@ctitle(4, 8)'
      }]
    }
  });
});
```

### 4.3 文档详情接口
```typescript
Mock.mock(new RegExp(`${API_PREFIX}/documents/\\d+`), 'get', () => {
  return Mock.mock({
    'code': 200,
    'message': 'success',
    'data': {
      'id': '@id',
      'title': '@ctitle(10, 20)',
      'type|1': ['招标文件', '投标文件', '技术方案'],
      'status|1': ['草稿', '审核中', '已定稿'],
      'content': '@cparagraph(10, 20)',
      'creator': {
        'id': '@id',
        'name': '@cname',
        'avatar': '@image("200x200")'
      },
      'createTime': '@datetime',
      'updateTime': '@datetime',
      'reviewers|2-4': [{
        'id': '@id',
        'name': '@cname',
        'avatar': '@image("200x200")',
        'status|1': ['待审核', '已通过', '已拒绝']
      }],
      'attachments|0-3': [{
        'id': '@id',
        'name': '@ctitle(5, 10).@pick(["doc", "pdf", "xlsx"])',
        'size': '@integer(100, 10000)',
        'uploadTime': '@datetime'
      }]
    }
  });
});
```

### 4.4 统一响应处理
```typescript
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const responseHandler = <T>(response: ApiResponse<T>) => {
  if (response.code === 200) {
    return response.data;
  }
  throw new Error(response.message);
};

const errorHandler = (error: any) => {
  // 统一错误处理
  console.error('API Error:', error);
  notification.error({
    message: '操作失败',
    description: error.message || '请稍后重试'
  });
  return Promise.reject(error);
};
```

## 五、路由权限控制

### 5.1 路由配置
```typescript
interface RouteConfig {
  path: string;
  component: React.ComponentType;
  permission?: string | string[];
  children?: RouteConfig[];
}

const routes: RouteConfig[] = [
  {
    path: '/projects',
    component: ProjectList,
    permission: 'project:view'
  },
  {
    path: '/projects/create',
    component: ProjectCreate,
    permission: 'project:create'
  },
  {
    path: '/projects/:id',
    component: ProjectDetail,
    permission: 'project:view'
  }
];
```

### 5.2 权限路由组件
```typescript
interface PrivateRouteProps {
  permission?: string | string[];
  component: React.ComponentType;
  path: string;
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({
  permission,
  component: Component,
  ...rest
}) => {
  const { isAuthenticated, hasPermission } = useAuth();
  
  return (
    <Route
      {...rest}
      render={props => {
        if (!isAuthenticated) {
          return <Redirect to="/login" />;
        }
        
        if (permission && !hasPermission(permission)) {
          return <Redirect to="/403" />;
        }
        
        return <Component {...props} />;
      }}
    />
  );
};
```

## 六、按钮级权限控制

### 6.1 权限按钮组件
```typescript
interface AuthButtonProps extends ButtonProps {
  permission: string | string[];
}

const AuthButton: React.FC<AuthButtonProps> = ({
  permission,
  children,
  ...props
}) => {
  const { hasPermission } = useAuth();
  
  if (!hasPermission(permission)) {
    return null;
  }
  
  return <Button {...props}>{children}</Button>;
};
```

### 6.2 菜单权限控制
```typescript
interface MenuItem {
  key: string;
  label: string;
  icon?: React.ReactNode;
  permission?: string | string[];
  children?: MenuItem[];
}

const filterMenuItems = (items: MenuItem[], permissions: string[]): MenuItem[] => {
  return items.filter(item => {
    if (item.permission) {
      if (Array.isArray(item.permission)) {
        if (!item.permission.every(p => permissions.includes(p))) {
          return false;
        }
      } else if (!permissions.includes(item.permission)) {
        return false;
      }
    }
    
    if (item.children) {
      item.children = filterMenuItems(item.children, permissions);
    }
    
    return true;
  });
};
```

## 七、Mock数据生成规则

### 7.1 基础数据生成
```typescript
const mockDataRules = {
  // 用户相关
  'user': {
    'id': '@id',
    'name': '@cname',
    'username': /[a-z]{3,8}/,
    'email': '@email',
    'phone': /1[3-9]\d{9}/,
    'avatar': '@image("100x100")',
    'department': '@ctitle(4, 8)',
    'position': '@ctitle(4, 8)'
  },
  
  // 项目相关
  'project': {
    'id': '@id',
    'name': '@ctitle(10, 20)',
    'code': /BID-\d{8}-\d{4}/,
    'type|1': ['产品类', '服务类', '工程类'],
    'status|1': ['进行中', '已完成', '已废标'],
    'amount': '@float(10000, 1000000, 2, 2)',
    'createTime': '@datetime',
    'deadline': '@datetime'
  },
  
  // 文档相关
  'document': {
    'id': '@id',
    'title': '@ctitle(10, 20)',
    'type|1': ['招标文件', '投标文件', '技术方案'],
    'status|1': ['草稿', '审核中', '已定稿'],
    'content': '@cparagraph(10, 20)',
    'version': '@increment(1)'
  }
};
```

### 7.2 复杂数据生成
```typescript
const generateMockData = (template: any, count: number = 1) => {
  return Mock.mock({
    [`data|${count}`]: [template]
  }).data;
};

// 生成项目数据
const projects = generateMockData(mockDataRules.project, 10);

// 生成带有团队成员的项目数据
const projectsWithTeam = projects.map(project => ({
  ...project,
  'team|3-6': [mockDataRules.user]
}));
```

## 八、接口规范

### 8.1 接口响应格式
```typescript
interface ApiResponse<T> {
  code: number;      // 状态码
  message: string;   // 提示信息
  data: T;          // 响应数据
  timestamp: number; // 时间戳
}

// 分页数据格式
interface PageResponse<T> {
  total: number;     // 总条数
  list: T[];        // 数据列表
  page: number;     // 当前页码
  pageSize: number; // 每页条数
}
```

### 8.2 Mock接口列表
```typescript
const mockApis = [
  // 认证相关
  {
    url: '/api/v1/auth/login',
    method: 'post',
    response: (params: any) => ({
      code: 200,
      message: 'success',
      data: {
        token: '@guid',
        user: mockDataRules.user
      }
    })
  },
  
  // 项目相关
  {
    url: '/api/v1/projects',
    method: 'get',
    response: (params: any) => ({
      code: 200,
      message: 'success',
      data: {
        total: '@integer(100, 200)',
        list: generateMockData(mockDataRules.project, params.pageSize)
      }
    })
  },
  
  // 文档相关
  {
    url: '/api/v1/documents',
    method: 'get',
    response: (params: any) => ({
      code: 200,
      message: 'success',
      data: {
        total: '@integer(50, 100)',
        list: generateMockData(mockDataRules.document, params.pageSize)
      }
    })
  }
];
```

### 8.3 接口错误码
```typescript
enum ApiErrorCode {
  SUCCESS = 200,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  VALIDATION_ERROR = 422,
  INTERNAL_ERROR = 500
}

const errorMessages = {
  [ApiErrorCode.UNAUTHORIZED]: '请先登录',
  [ApiErrorCode.FORBIDDEN]: '没有操作权限',
  [ApiErrorCode.NOT_FOUND]: '资源不存在',
  [ApiErrorCode.VALIDATION_ERROR]: '参数验证失败',
  [ApiErrorCode.INTERNAL_ERROR]: '服务器内部错误'
};
``` 
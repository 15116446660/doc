package com.biaoshu.documentreview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

public class OnlyOfficeDTO {

    @Data
    @Schema(description = "OnlyOffice编辑器配置")
    public static class EditorConfig {
        private Document document;
        private EditorConfig editorConfig;
        private String token;
        private String type = "desktop";
        private int height = 800;
        private int width = 1200;
    }

    @Data
    @Schema(description = "文档对象")
    public static class Document {
        private String title;
        private String url;
        private String fileType;
        private String key;
        private Permissions permissions;
    }

    @Data
    @Schema(description = "编辑器配置内部对象")
    @JsonProperty("editorConfig")
    public static class EditorConfigInternal {
        private String mode = "edit";
        private String callbackUrl;
        private User user;
        private Customization customization;
    }

    @Data
    @Schema(description = "用户对象")
    public static class User {
        private String id;
        private String name;
    }

    @Data
    @Schema(description = "权限对象")
    public static class Permissions {
        private boolean edit;
        private boolean download = true;
        private boolean print = true;
        private boolean review = true;
    }

    @Data
    @Schema(description = "自定义对象")
    public static class Customization {
        private boolean forcesave;
        private Goback goback;
    }

    @Data
    @Schema(description = "返回对象")
    public static class Goback {
        private String url;
    }

    @Data
    @Schema(description = "OnlyOffice回调对象")
    public static class Callback {
        private List<Action> actions;
        private String key;
        private int status;
        private String url;
        private String userdata;
        private List<String> users;
    }

    @Data
    @Schema(description = "回调中的Action对象")
    public static class Action {
        private String type;
        private String userid;
    }
}

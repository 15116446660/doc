# AI对话助手消息编辑功能

## 功能概述

AI对话助手现在支持编辑已发送的用户消息，并基于编辑后的内容重新生成AI回复。此功能可以帮助用户在发现错误或需要补充信息时，无需重新开始对话。

## 使用方法

1. 将鼠标悬停在任何用户消息上，会显示操作按钮
2. 点击"编辑"按钮（铅笔图标）开始编辑
3. 在文本框中修改消息内容（文本框会根据内容自动调整大小）
4. 点击"发送"按钮提交修改，或点击"取消"放弃修改
5. 发送后，系统会保留原有用户消息（但内容已更新），并基于编辑后的内容重新生成AI回复

## 技术实现

### 数据流程

![编辑消息流程图](https://mermaid.ink/img/pako:eNqNVMtuwjAQ_BXLp1YKpJRCexyQWqkSh_ZQVYcqh8XZBKskdmQ7QBH8e9cJkABtD-bgyM7s7Mzaew6FkpQnfKmNMU9YgkGHuXXcGEyYM6ZEpWXOJE_4XYPGsg18NKpkHmutkaVYQGHsI3dMFhueg7G2cMAewBteglbSASvBAjgH7so15OAcbLgDs4ZcQcE1WAnAJDXMqSmLBW_AOq7AouNQooJ72eRcg0MHjjt4gl-lKR3MwaKaS5fB1WqVgV9z0va5fLG0rNK84A7LJUynkwldTiYzmE9BVg6mYLl-gt-8cYLrcs5LB79KLqEwegO_pIbC6A0YhF-VhgP4CvdcrUEhtEa7Z_iiLX-E71w9woOxJRR8Axm3-wbfuSohYyWs5Aq-wY1RkMkSMrOBn7KAQpawYmoDN0ZDJguYTqaXs-lsNsngur-lf2U6nV7CbAazq8klfJYGMpnDkpcNb3nC-6IXCb9Dq8waJXFctNw6EtBJnWNbciPb9h3HyqBhDe9QO5ZRDEpdNth5L42rqGFsqfkvD6ElGq1BspY7tm1QcYtsReNRMOesI8secUdvxBx_dyTETvGEv0dVYG7Q7QNdhAFpSAd07NhWssQdidhN-SPhH2VBi1C8Q8dVw_YxvUTDdm_RkyThX4wtaFI7oxcoHSrHqoYG0-4jqoaOhF-dnJ6eHn84OTk7Pjo7Pj0_Ozo6Pj8_Pjo5Pzk_OTs7Oz85Pfr44f2Hjx9P3707-_z2-Pzk5O2b07Ozt2_en52fvX_z5u2LP7Z1ZV0)

### 组件交互

1. **用户界面**：ChatBubbleList组件提供编辑界面和用户交互
2. **控制层**：Chat组件处理事件并调用相应的逻辑函数
3. **业务逻辑**：useChat钩子提供编辑消息的核心功能
4. **数据持久化**：编辑后的会话自动保存到IndexedDB

### 核心功能

1. **开始编辑**：将消息状态设置为"editing"，显示编辑界面
2. **保存编辑**：
   - 保存原始消息内容（用于潜在的恢复功能）
   - 更新用户消息内容
   - 标记消息为"已编辑"
   - 删除该消息之后的所有消息
   - 创建新的AI回复消息并生成回复
3. **取消编辑**：恢复消息状态为"completed"，不做任何更改

## 最新更新

- **UI改进**：文本输入框现在会根据内容自动调整大小，最小1行，最大10行
- **按钮文案**：将"保存"按钮改为"发送"，更符合用户习惯
- **流程优化**：编辑后不再创建新的用户消息，而是直接更新原有消息内容

## 注意事项

- 编辑消息会删除该消息之后的所有对话内容，包括AI回复和后续的用户消息
- 编辑功能仅适用于用户消息，不能编辑AI回复
- 编辑过的消息会显示"(已编辑)"标记
- 系统会保存原始消息内容，为将来可能的撤销功能做准备

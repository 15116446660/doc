export interface TemplateType {
  id: number
  typeCode: string
  typeName: string
  parentId?: number
  children?: TemplateType[]
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
  status?: 'active' | 'inactive'
  sort?: number
  remark?: string
  
  // Additional fields from API
  tenantId?: number
  createUid?: number
  updateUid?: number
}

export interface Template {
  id: number | string
  templateCode: string
  templateName: string
  typeId: number | string
  content: string
  version: string
  status: 'draft' | 'published' | 'archived'
  createTime: string
  updateTime: string
  createBy: string
  updateBy: string
  remark?: string
  type?: TemplateType
} 
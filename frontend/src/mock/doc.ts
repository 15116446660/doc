import type { MockMethod } from 'vite-plugin-mock'
import type { DocumentCollaborator } from '@/types/document'

const collaborators: DocumentCollaborator[] = [
  {
    userId: 46,
    userName: '向培',
    orgName: '研发管理部',
    position: '开发',
    picture: '/image/2025/05/21/1b280a261aba4faf82a3abb6b897c588photo(5).png',
    identity: 'DIRECTOR',
    selected: 1,
    down: 1,
    edit: 1,
    upload: 1,
    del: 1,
    print: 1,
    comment: 1,
    copy: 1
  },
  {
    userId: 49,
    userName: '测试用户',
    orgName: '景嘉微',
    position: '测试',
    picture: '',
    identity: null,
    selected: 0,
    down: 0,
    edit: 0,
    upload: 0,
    del: 0,
    print: 0,
    comment: 0,
    copy: 0
  },
  {
    userId: 150,
    userName: '李晓明',
    orgName: '电子事业部',
    position: '产品经理',
    picture: '',
    identity: null,
    selected: 1,
    down: 0,
    edit: 1,
    upload: 0,
    del: 0,
    print: 1,
    comment: 1,
    copy: 1
  }
]

export default [
  {
    url: '/api/document/collaborators',
    method: 'get',
    response: ({ query }: { query: { documentId: string } }) => {
      console.log(`[Mock] Getting collaborators for document ${query.documentId}`)
      return {
        code: 200,
        msg: '操作成功',
        data: collaborators
      }
    }
  },
  {
    url: '/api/document/collaborators/update',
    method: 'post',
    response: ({
      query,
      body
    }: {
      query: { documentId: string }
      body: { collaborators: DocumentCollaborator[] }
    }) => {
      console.log(`[Mock] Updating collaborators for document ${query.documentId}`, body.collaborators)
      return {
        code: 200,
        msg: '更新成功'
      }
    }
  }
] as MockMethod[] 
import { ref, shallowRef } from 'vue'

interface DialogInstance {
  name: string
  visible: boolean
  props?: Record<string, any>
  events?: Record<string, Function>
}

export function useDialog() {
  const dialogs = ref<DialogInstance[]>([])

  /**
   * 打开对话框
   * @param name 对话框名称
   * @param props 传递给对话框的属性
   * @param events 传递给对话框的事件回调
   */
  const open = (name: string, props?: Record<string, any>, events?: Record<string, Function>) => {
    const existingDialog = dialogs.value.find(dialog => dialog.name === name)
    
    if (existingDialog) {
      existingDialog.visible = true
      if (props) {
        existingDialog.props = { ...existingDialog.props, ...props }
      }
      if (events) {
        existingDialog.events = { ...existingDialog.events, ...events }
      }
    } else {
      dialogs.value.push({
        name,
        visible: true,
        props,
        events
      })
    }
  }

  /**
   * 关闭对话框
   * @param name 对话框名称
   */
  const close = (name: string) => {
    const dialogIndex = dialogs.value.findIndex(dialog => dialog.name === name)
    if (dialogIndex > -1) {
      dialogs.value[dialogIndex].visible = false
    }
  }

  /**
   * 检查对话框是否可见
   * @param name 对话框名称
   * @returns 是否可见
   */
  const isVisible = (name: string) => {
    const dialog = dialogs.value.find(dialog => dialog.name === name)
    return dialog ? dialog.visible : false
  }

  /**
   * 获取对话框属性
   * @param name 对话框名称
   * @returns 对话框属性
   */
  const getProps = (name: string) => {
    const dialog = dialogs.value.find(dialog => dialog.name === name)
    return dialog?.props || {}
  }

  /**
   * 获取对话框事件回调
   * @param name 对话框名称
   * @param eventName 事件名称
   * @returns 事件回调函数
   */
  const getEventCallback = (name: string, eventName: string) => {
    const dialog = dialogs.value.find(dialog => dialog.name === name)
    return dialog?.events?.[eventName]
  }

  /**
   * 触发对话框事件
   * @param name 对话框名称
   * @param eventName 事件名称
   * @param args 事件参数
   */
  const emit = (name: string, eventName: string, ...args: any[]) => {
    const callback = getEventCallback(name, eventName)
    if (callback && typeof callback === 'function') {
      return callback(...args)
    }
  }

  return {
    dialogs,
    open,
    close,
    isVisible,
    getProps,
    getEventCallback,
    emit
  }
}

// 创建全局单例
const dialogInstance = useDialog()
export default dialogInstance 
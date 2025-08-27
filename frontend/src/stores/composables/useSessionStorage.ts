import { ref, watch, Ref } from 'vue'

export function useSessionStorage<T>(key: string, defaultValue: T): {
  value: Ref<T>
  setValue: (newValue: T) => void
  removeValue: () => void
} {
  const storedValue = sessionStorage.getItem(key)
  const initialValue = storedValue ? JSON.parse(storedValue) : defaultValue

  const value = ref<T>(initialValue)

  const setValue = (newValue: T) => {
    value.value = newValue
    sessionStorage.setItem(key, JSON.stringify(newValue))
  }

  const removeValue = () => {
    value.value = defaultValue
    sessionStorage.removeItem(key)
  }

  // 监听值变化，自动同步到sessionStorage
  watch(value, (newValue) => {
    sessionStorage.setItem(key, JSON.stringify(newValue))
  }, { deep: true })

  return {
    value,
    setValue,
    removeValue
  }
}

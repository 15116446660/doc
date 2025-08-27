import { ref, reactive, computed, readonly } from 'vue'
import { useSessionStorage } from './composables/useSessionStorage'
import { useAuthStore } from './useAuthStore'

// Placeholder Types and API
// These should be moved to appropriate files in src/types and src/api later
interface ReviewTask {
  id: number;
  taskName: string;
  status: string;
  priority: string;
  businessType: string;
  creatorId: number;
  assignedExperts?: { userId: number }[];
}
interface TaskFilter {
  status: string;
  priority: string;
  businessType: string;
  assignedTo: string;
  dateRange: any[];
}
interface TaskStatistics {
  total: number;
  pending: number;
  inProgress: number;
}

const reviewApi = {
  getTasks: async (params?: any) => {
    console.log('Mock getTasks with', params);
    await new Promise(resolve => setTimeout(resolve, 500));
    const tasks = [
      { id: 1, taskName: 'Task 1', status: 'IN_PROGRESS', priority: 'HIGH', businessType: 'CONTRACT', creatorId: 1 },
      { id: 2, taskName: 'Task 2', status: 'PENDING', priority: 'MEDIUM', businessType: 'LEGAL', creatorId: 2 },
    ];
    return { data: { content: tasks } };
  },
  createTask: async (taskData: Partial<ReviewTask>) => {
    console.log('Mock createTask with', taskData);
    await new Promise(resolve => setTimeout(resolve, 500));
    const newTask = { id: Date.now(), status: 'PENDING', ...taskData };
    return { data: newTask };
  },
  updateTask: async (taskId: number, updates: Partial<ReviewTask>) => {
    console.log('Mock updateTask with', taskId, updates);
    await new Promise(resolve => setTimeout(resolve, 500));
    return { data: { id: taskId, ...updates } };
  },
  getTaskDetail: async (taskId: number) => {
    console.log('Mock getTaskDetail for', taskId);
    await new Promise(resolve => setTimeout(resolve, 500));
    return { data: { id: taskId, taskName: `Task ${taskId} Details`, status: 'IN_PROGRESS', priority: 'HIGH', businessType: 'CONTRACT', creatorId: 1 } };
  },
  getStatistics: async () => {
    console.log('Mock getStatistics');
    await new Promise(resolve => setTimeout(resolve, 500));
    return { data: { total: 10, pending: 5, inProgress: 5 } };
  }
};
// End Placeholder


const tasks = ref<ReviewTask[]>([])
const currentTask = ref<ReviewTask | null>(null)
const taskFilter = reactive<TaskFilter>({
  status: '',
  priority: '',
  businessType: '',
  assignedTo: '',
  dateRange: []
})
const isLoading = ref(false)
const statistics = ref<TaskStatistics | null>(null)

const { value: sessionCurrentTask, setValue: setSessionCurrentTask } = useSessionStorage('current_review_task', null)

export function useReviewStore() {
  const filteredTasks = computed(() => {
    return tasks.value.filter(task => {
      if (taskFilter.status && task.status !== taskFilter.status) return false
      if (taskFilter.priority && task.priority !== taskFilter.priority) return false
      if (taskFilter.businessType && task.businessType !== taskFilter.businessType) return false
      if (taskFilter.assignedTo && !task.assignedExperts?.some(expert => expert.userId === Number(taskFilter.assignedTo))) return false
      return true
    })
  })

  const tasksByStatus = computed(() => {
    return tasks.value.reduce((acc, task) => {
      const status = task.status as keyof typeof acc;
      acc[status] = (acc[status] || 0) + 1
      return acc
    }, {} as Record<string, number>)
  })

  const myTasks = computed(() => {
    const { user } = useAuthStore()
    if (!user.value) return [];
    return tasks.value.filter(task =>
      task.creatorId === user.value?.id ||
      task.assignedExperts?.some(expert => expert.userId === user.value?.id)
    )
  })

  const fetchTasks = async (params?: any) => {
    isLoading.value = true
    try {
      const response = await reviewApi.getTasks(params)
      tasks.value = response.data.content
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    } finally {
      isLoading.value = false
    }
  }

  const createTask = async (taskData: Partial<ReviewTask>) => {
    isLoading.value = true
    try {
      const response = await reviewApi.createTask(taskData)
      const newTask = response.data as ReviewTask
      tasks.value.unshift(newTask)
      return { success: true, data: newTask }
    } catch (error: any) {
      return { success: false, error: error.message }
    } finally {
      isLoading.value = false
    }
  }

  const updateTask = async (taskId: number, updates: Partial<ReviewTask>) => {
    try {
      const response = await reviewApi.updateTask(taskId, updates)
      const updatedTask = response.data as ReviewTask

      const index = tasks.value.findIndex(task => task.id === taskId)
      if (index !== -1) {
        tasks.value[index] = updatedTask
      }

      if (currentTask.value?.id === taskId) {
        currentTask.value = updatedTask
        setSessionCurrentTask(updatedTask)
      }

      return { success: true, data: updatedTask }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const setCurrentTask = (task: ReviewTask | null) => {
    currentTask.value = task
    setSessionCurrentTask(task)
  }

  const fetchTaskDetail = async (taskId: number) => {
    isLoading.value = true
    try {
      const response = await reviewApi.getTaskDetail(taskId)
      const task = response.data as ReviewTask
      setCurrentTask(task)
      return { success: true, data: task }
    } catch (error: any) {
      return { success: false, error: error.message }
    } finally {
      isLoading.value = false
    }
  }

  const updateFilter = (newFilter: Partial<TaskFilter>) => {
    Object.assign(taskFilter, newFilter)
  }

  const resetFilter = () => {
    Object.assign(taskFilter, {
      status: '',
      priority: '',
      businessType: '',
      assignedTo: '',
      dateRange: []
    })
  }

  const fetchStatistics = async () => {
    try {
      const response = await reviewApi.getStatistics()
      statistics.value = response.data
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const initialize = () => {
    if (sessionCurrentTask.value) {
      currentTask.value = sessionCurrentTask.value as ReviewTask | null;
    }
  }

  return {
    tasks: readonly(tasks),
    currentTask: readonly(currentTask),
    taskFilter: readonly(taskFilter),
    isLoading: readonly(isLoading),
    statistics: readonly(statistics),
    filteredTasks,
    tasksByStatus,
    myTasks,
    fetchTasks,
    createTask,
    updateTask,
    setCurrentTask,
    fetchTaskDetail,
    updateFilter,
    resetFilter,
    fetchStatistics,
    initialize
  }
}

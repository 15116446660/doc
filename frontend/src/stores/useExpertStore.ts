import { ref, computed, readonly } from 'vue'
import type { Ref } from 'vue'

// Placeholder Types and API
interface Expert {
    userId: number;
    userName: string;
    isAvailable: boolean;
    currentWorkload: number;
    maxConcurrentReviews: number;
    skillTags?: string[];
}
interface WorkloadStats {
    totalExperts: number;
    availableExperts: number;
    overloadedExperts: number;
    averageWorkload: number;
}
interface ExpertRecommendation {
    userId: number;
    userName: string;
    overallScore: number;
}

const expertApi = {
    getExperts: async (params?: any) => {
        console.log('Mock getExperts with', params);
        await new Promise(resolve => setTimeout(resolve, 500));
        const experts: Expert[] = [
            { userId: 101, userName: '张专家', isAvailable: true, currentWorkload: 2, maxConcurrentReviews: 5, skillTags: ['合同法', '商务谈判'] },
            { userId: 102, userName: '李专家', isAvailable: false, currentWorkload: 5, maxConcurrentReviews: 5, skillTags: ['技术规范'] },
        ];
        return { data: { content: experts } };
    },
    getRecommendations: async (requirements: any) => {
        console.log('Mock getRecommendations with', requirements);
        await new Promise(resolve => setTimeout(resolve, 500));
        const recommendations: ExpertRecommendation[] = [
            { userId: 101, userName: '张专家', overallScore: 95.5 },
        ];
        return { data: { recommendations } };
    },
    assignExperts: async (payload: { taskId: number, assignments: any[] }) => {
        console.log('Mock assignExperts with', payload);
        await new Promise(resolve => setTimeout(resolve, 500));
        return { data: { success: true } };
    },
    getWorkloadStats: async () => {
        console.log('Mock getWorkloadStats');
        await new Promise(resolve => setTimeout(resolve, 500));
        const stats: WorkloadStats = { totalExperts: 50, availableExperts: 35, overloadedExperts: 8, averageWorkload: 65.5 };
        return { data: stats };
    }
};
// End Placeholder


const experts: Ref<Expert[]> = ref([])
const workloadStats: Ref<WorkloadStats | null> = ref(null)
const recommendations: Ref<ExpertRecommendation[]> = ref([])
const isLoading = ref(false)

export function useExpertStore() {
  const availableExperts = computed(() =>
    experts.value.filter(expert => expert.isAvailable && expert.currentWorkload < expert.maxConcurrentReviews)
  )

  const expertsBySkill = computed(() => (skill: string) =>
    experts.value.filter(expert => expert.skillTags?.includes(skill))
  )

  const fetchExperts = async (params?: any) => {
    isLoading.value = true
    try {
      const response = await expertApi.getExperts(params)
      experts.value = response.data.content
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    } finally {
      isLoading.value = false
    }
  }

  const getRecommendations = async (requirements: any) => {
    try {
      const response = await expertApi.getRecommendations(requirements)
      recommendations.value = response.data.recommendations
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const assignExperts = async (taskId: number, assignments: any[]) => {
    try {
      const response = await expertApi.assignExperts({ taskId, assignments })
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const fetchWorkloadStats = async () => {
    try {
      const response = await expertApi.getWorkloadStats()
      workloadStats.value = response.data
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  return {
    experts: readonly(experts),
    workloadStats: readonly(workloadStats),
    recommendations: readonly(recommendations),
    isLoading: readonly(isLoading),
    availableExperts,
    expertsBySkill,
    fetchExperts,
    getRecommendations,
    assignExperts,
    fetchWorkloadStats
  }
}

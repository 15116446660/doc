import { ref, reactive, computed, readonly } from 'vue'
import type { Ref } from 'vue'

// Placeholder Types and API
// These should be moved to appropriate files in src/types and src/api later
interface Issue {
    id: number;
    title: string;
    status: string;
    severity: string;
    category: string;
    assignedToId: number | null;
    reviewTaskId: number | null;
}
interface IssueFilter {
  severity: string;
  category: string;
  status: string;
  assignedTo: string;
  reviewTaskId: number | null;
}
interface IssueStatistics {
    total: number;
    open: number;
    inProgress: number;
    resolved: number;
}

const issueApi = {
    getIssues: async (params?: any) => {
        console.log('Mock getIssues with', params);
        await new Promise(resolve => setTimeout(resolve, 500));
        const issues = [
            { id: 1, title: 'Issue 1', status: 'OPEN', severity: 'HIGH', category: 'CONTENT', assignedToId: 1, reviewTaskId: 1 },
            { id: 2, title: 'Issue 2', status: 'RESOLVED', severity: 'MEDIUM', category: 'FORMAT', assignedToId: 2, reviewTaskId: 1 },
        ];
        return { data: { content: issues } };
    },
    createIssue: async (issueData: Partial<Issue>) => {
        console.log('Mock createIssue with', issueData);
        await new Promise(resolve => setTimeout(resolve, 500));
        const newIssue = { id: Date.now(), status: 'OPEN', ...issueData };
        return { data: newIssue };
    },
    updateIssue: async (issueId: number, updates: Partial<Issue>) => {
        console.log('Mock updateIssue with', issueId, updates);
        await new Promise(resolve => setTimeout(resolve, 500));
        return { data: { id: issueId, ...updates } };
    },
    batchUpdate: async (payload: { issueIds: number[], updates: Partial<Issue> }) => {
        console.log('Mock batchUpdate with', payload);
        await new Promise(resolve => setTimeout(resolve, 500));
        return { data: { success: true, count: payload.issueIds.length } };
    },
    detectDuplicates: async (issueData: Partial<Issue>) => {
        console.log('Mock detectDuplicates with', issueData);
        await new Promise(resolve => setTimeout(resolve, 500));
        return { data: { isDuplicate: false, similarIssues: [] } };
    },
};
// End Placeholder

const issues: Ref<Issue[]> = ref([])
const currentIssue: Ref<Issue | null> = ref(null)
const issueFilter = reactive<IssueFilter>({
  severity: '',
  category: '',
  status: '',
  assignedTo: '',
  reviewTaskId: null
})
const isLoading = ref(false)
const statistics: Ref<IssueStatistics | null> = ref(null)

export function useIssueStore() {
  const filteredIssues = computed(() => {
    return issues.value.filter(issue => {
      if (issueFilter.severity && issue.severity !== issueFilter.severity) return false
      if (issueFilter.category && issue.category !== issueFilter.category) return false
      if (issueFilter.status && issue.status !== issueFilter.status) return false
      if (issueFilter.assignedTo && issue.assignedToId !== Number(issueFilter.assignedTo)) return false
      if (issueFilter.reviewTaskId && issue.reviewTaskId !== issueFilter.reviewTaskId) return false
      return true
    })
  })

  const issuesByStatus = computed(() => {
    return issues.value.reduce((acc, issue) => {
      const status = issue.status as keyof typeof acc;
      acc[status] = (acc[status] || 0) + 1
      return acc
    }, {} as Record<string, number>)
  })

  const criticalIssues = computed(() =>
    issues.value.filter(issue => issue.severity === 'CRITICAL' && issue.status === 'OPEN')
  )

  const fetchIssues = async (params?: any) => {
    isLoading.value = true
    try {
      const response = await issueApi.getIssues(params)
      issues.value = response.data.content
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    } finally {
      isLoading.value = false
    }
  }

  const createIssue = async (issueData: Partial<Issue>) => {
    try {
      const response = await issueApi.createIssue(issueData)
      const newIssue = response.data as Issue
      issues.value.unshift(newIssue)
      return { success: true, data: newIssue }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const updateIssue = async (issueId: number, updates: Partial<Issue>) => {
    try {
      const response = await issueApi.updateIssue(issueId, updates)
      const updatedIssue = response.data as Issue

      const index = issues.value.findIndex(issue => issue.id === issueId)
      if (index !== -1) {
        issues.value[index] = updatedIssue
      }

      if (currentIssue.value?.id === issueId) {
        currentIssue.value = updatedIssue
      }

      return { success: true, data: updatedIssue }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const batchUpdateIssues = async (issueIds: number[], updates: Partial<Issue>) => {
    try {
      const response = await issueApi.batchUpdate({ issueIds, updates })

      issueIds.forEach(id => {
        const index = issues.value.findIndex(issue => issue.id === id)
        if (index !== -1) {
          issues.value[index] = { ...issues.value[index], ...updates }
        }
      })

      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const detectDuplicates = async (issueData: Partial<Issue>) => {
    try {
      const response = await issueApi.detectDuplicates(issueData)
      return { success: true, data: response.data }
    } catch (error: any) {
      return { success: false, error: error.message }
    }
  }

  const updateFilter = (newFilter: Partial<IssueFilter>) => {
    Object.assign(issueFilter, newFilter)
  }

  const resetFilter = () => {
    Object.assign(issueFilter, {
      severity: '',
      category: '',
      status: '',
      assignedTo: '',
      reviewTaskId: null
    })
  }

  return {
    issues: readonly(issues),
    currentIssue: readonly(currentIssue),
    issueFilter: readonly(issueFilter),
    isLoading: readonly(isLoading),
    statistics: readonly(statistics),
    filteredIssues,
    issuesByStatus,
    criticalIssues,
    fetchIssues,
    createIssue,
    updateIssue,
    batchUpdateIssues,
    detectDuplicates,
    updateFilter,
    resetFilter
  }
}

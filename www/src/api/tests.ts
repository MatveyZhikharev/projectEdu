import apiClient from './index'

// Test related interfaces
export interface Answer {
  id: number
  answerText: string
  isRight?: boolean // Only visible after submission in some implementations
}

export interface Question {
  id: number
  questionText: string
  answers: Answer[]
}

export interface TestData {
  id: number
  passPercent: number
  questions: Question[]
}

export interface TestSubmitRequest {
  testId: number
  answers: Record<number, number> // questionId -> answerId
}

export interface TestResultResponse {
  passed: boolean
  score: number
  correctCount: number
  totalQuestions: number
}

/**
 * Tests API service
 * 
 * Note: These endpoints are not yet implemented in the backend.
 * The frontend uses mock data until the backend implements:
 * - GET /api/tests/{testId} - Get test with questions
 * - POST /api/tests/{testId}/submit - Submit test answers
 */
export const testsApi = {
  /**
   * Get test data by ID
   * @param testId - Test ID from block.testId
   * @returns Test data with questions and answers
   */
  getTest(testId: number) {
    return apiClient.get<TestData>(`/tests/${testId}`)
  },

  /**
   * Submit test answers
   * @param testId - Test ID
   * @param answers - Map of questionId to selected answerId
   * @returns Test result with score and pass status
   */
  submitTest(testId: number, answers: Record<number, number>) {
    return apiClient.post<TestResultResponse>(`/tests/${testId}/submit`, { answers })
  },

  /**
   * Generate mock test data for development
   * Used when backend API is not available
   */
  getMockTestData(testId: number): TestData {
    return {
      id: testId,
      passPercent: 70,
      questions: [
        {
          id: 1,
          questionText: 'Какой инструмент чаще всего используется для откручивания винтов?',
          answers: [
            { id: 1, answerText: 'Молоток', isRight: false },
            { id: 2, answerText: 'Отвёртка', isRight: true },
            { id: 3, answerText: 'Пассатижи', isRight: false },
            { id: 4, answerText: 'Гаечный ключ', isRight: false },
          ]
        },
        {
          id: 2,
          questionText: 'Что нужно сделать перед началом ремонта электроприбора?',
          answers: [
            { id: 5, answerText: 'Включить его в сеть', isRight: false },
            { id: 6, answerText: 'Отключить от электропитания', isRight: true },
            { id: 7, answerText: 'Намочить руки', isRight: false },
            { id: 8, answerText: 'Ничего не делать', isRight: false },
          ]
        },
        {
          id: 3,
          questionText: 'Для чего используется мультиметр?',
          answers: [
            { id: 9, answerText: 'Для измерения давления', isRight: false },
            { id: 10, answerText: 'Для измерения электрических параметров', isRight: true },
            { id: 11, answerText: 'Для резки проводов', isRight: false },
            { id: 12, answerText: 'Для пайки', isRight: false },
          ]
        }
      ]
    }
  }
}

import { describe, expect, it } from 'vitest'

import { businessErrorMessages, getBusinessErrorMessage } from './errorMessages'

describe('business error messages', () => {
  it('[FR-005] maps known business codes to actionable messages', () => {
    expect(getBusinessErrorMessage('A0000')).toBe(businessErrorMessages.A0000)
    expect(getBusinessErrorMessage('B0000')).toBe(businessErrorMessages.B0000)
    expect(getBusinessErrorMessage('C0000')).toBe(businessErrorMessages.C0000)
  })

  it('[AC-009] keeps an API message when it is provided', () => {
    expect(getBusinessErrorMessage('A0000', 'ISBN 已存在')).toBe('ISBN 已存在')
    expect(getBusinessErrorMessage('A0000', '   ')).toBe(businessErrorMessages.A0000)
    expect(getBusinessErrorMessage('UNKNOWN')).toBe(businessErrorMessages.B0000)
  })
})

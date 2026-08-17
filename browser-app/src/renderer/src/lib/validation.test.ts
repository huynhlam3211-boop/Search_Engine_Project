import { describe, it, expect } from 'vitest'
import {
  MIN_PASSWORD_LENGTH,
  passwordStrength,
  validateConfirmation,
  validatePassword,
  validateUsername
} from './validation'

describe('validateUsername', () => {
  it('accepts valid names', () => {
    expect(validateUsername('johndoe')).toBeNull()
    expect(validateUsername('user.name_01')).toBeNull()
    expect(validateUsername('a-b-c')).toBeNull()
  })

  it('rejects names that are too short or too long', () => {
    expect(validateUsername('ab')).toContain('3 characters')
    expect(validateUsername('x'.repeat(33))).toContain('32 characters')
  })

  it('rejects whitespace and unexpected characters', () => {
    expect(validateUsername('has white space')).toContain('Use only')
    expect(validateUsername('name/with/slash')).toContain('Use only')
    expect(validateUsername('josé')).toContain('Use only')
  })

  it('prompts for input on an empty field instead of reporting a bad format', () => {
    expect(validateUsername('   ')).toBe('Please enter a username.')
  })
})

describe('validatePassword', () => {
  it('accepts a long enough password', () => {
    expect(validatePassword('x'.repeat(MIN_PASSWORD_LENGTH))).toBeNull()
  })

  it('states how many characters are missing', () => {
    expect(validatePassword('shrt')).toContain('currently 4')
  })

  it('rejects a password that is too long', () => {
    expect(validatePassword('x'.repeat(201))).toContain('200 characters')
  })
})

describe('validateConfirmation', () => {
  it('reports NO error before anything is typed', () => {
    expect(validateConfirmation('longenoughpass', '')).toBeNull()
  })

  it('reports when the two entries differ', () => {
    expect(validateConfirmation('longenoughpass', 'otherpassword')).toContain('do not match')
  })

  it('reports nothing when they match', () => {
    expect(validateConfirmation('longenoughpass', 'longenoughpass')).toBeNull()
  })
})

describe('passwordStrength', () => {
  it('scores 0 below the minimum length', () => {
    expect(passwordStrength('shrt').score).toBe(0)
  })

  it('weighs length more than special characters', () => {
    const longSimple = passwordStrength('thelittlewhitecatinyard')
    const shortComplex = passwordStrength('Ab1!xyzq')

    expect(longSimple.score).toBeGreaterThan(shortComplex.score)
  })

  it('never exceeds the 0..3 scale', () => {
    const strong = passwordStrength('Very-Long-And-Complex-123456789!')
    expect(strong.score).toBe(3)
    expect(strong.label).toBe('Strong')
  })
})

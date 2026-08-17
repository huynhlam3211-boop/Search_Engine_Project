const USERNAME_PATTERN = /^[a-zA-Z0-9._-]{3,32}$/

export const MIN_PASSWORD_LENGTH = 8
export const MAX_PASSWORD_LENGTH = 200

export function validateUsername(username: string): string | null {
  const trimmed = username.trim()
  if (!trimmed) {
    return 'Please enter a username.'
  }
  if (trimmed.length < 3) {
    return 'Username needs at least 3 characters.'
  }
  if (trimmed.length > 32) {
    return 'Username can be at most 32 characters.'
  }
  if (!USERNAME_PATTERN.test(trimmed)) {
    return 'Use only unaccented letters, digits, dots, hyphens and underscores.'
  }
  return null
}

export function validatePassword(password: string): string | null {
  if (!password) {
    return 'Please enter a password.'
  }
  if (password.length < MIN_PASSWORD_LENGTH) {
    return `Password needs at least ${MIN_PASSWORD_LENGTH} characters (currently ${password.length}).`
  }
  if (password.length > MAX_PASSWORD_LENGTH) {
    return `Password can be at most ${MAX_PASSWORD_LENGTH} characters.`
  }
  return null
}

export function validateConfirmation(password: string, confirmation: string): string | null {
  if (!confirmation) {
    return null
  }
  return password === confirmation ? null : 'The two passwords do not match.'
}

export interface PasswordStrength {
  score: 0 | 1 | 2 | 3
  label: string
  hint: string
}

export function passwordStrength(password: string): PasswordStrength {
  if (password.length < MIN_PASSWORD_LENGTH) {
    return {
      score: 0,
      label: 'Too short',
      hint: `Needs at least ${MIN_PASSWORD_LENGTH} characters.`
    }
  }

  let score = 0
  if (password.length >= 12) score++
  if (password.length >= 16) score++
  const variety = [/[a-z]/, /[A-Z]/, /[0-9]/, /[^a-zA-Z0-9]/].filter((re) =>
    re.test(password)
  ).length
  if (variety >= 3) score++

  const capped = Math.min(3, score) as 0 | 1 | 2 | 3
  const labels = ['Weak', 'Fair', 'Good', 'Strong']
  const hints = [
    'A few words strung together is far stronger than one word with odd characters.',
    'Adding a few more characters helps much more than adding special characters.',
    'Good enough. Longer is still better.',
    'Great.'
  ]
  return { score: capped, label: labels[capped], hint: hints[capped] }
}

export function shannonEntropy(counts: number[]): number {
  const total = counts.reduce((sum, value) => sum + Math.max(0, value), 0)
  if (total <= 0) {
    return 0
  }
  let entropy = 0
  for (const value of counts) {
    if (value > 0) {
      const p = value / total
      entropy -= p * Math.log2(p)
    }
  }
  return entropy
}

export function normalizedEntropy(counts: number[]): number {
  const nonZero = counts.filter((value) => value > 0).length
  if (nonZero <= 1) {
    return 0
  }
  return shannonEntropy(counts) / Math.log2(nonZero)
}

export function headShare(counts: number[], k = 3): number {
  const total = counts.reduce((sum, value) => sum + Math.max(0, value), 0)
  if (total <= 0) {
    return 0
  }
  const head = [...counts]
    .sort((a, b) => b - a)
    .slice(0, k)
    .reduce((sum, value) => sum + value, 0)
  return head / total
}

export function coverageRatio(documents: number, distinctLinkTargets: number): number {
  if (distinctLinkTargets <= 0) {
    return 0
  }
  return Math.min(1, documents / distinctLinkTargets)
}

export function ratio(part: number, whole: number): number {
  if (!Number.isFinite(part) || !Number.isFinite(whole) || whole <= 0) {
    return 0
  }
  return part / whole
}

export function foldTail<T extends { label: string; count: number }>(
  items: T[],
  keep: number,
  otherLabel = 'other'
): { label: string; value: number }[] {
  const sorted = [...items].sort((a, b) => b.count - a.count)
  const head = sorted.slice(0, keep).map((item) => ({ label: item.label, value: item.count }))
  const tail = sorted.slice(keep).reduce((sum, item) => sum + item.count, 0)
  return tail > 0 ? [...head, { label: otherLabel, value: tail }] : head
}

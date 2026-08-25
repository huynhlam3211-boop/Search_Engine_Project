import { describe, it, expect } from 'vitest'
import {
  coverageRatio,
  foldTail,
  headShare,
  normalizedEntropy,
  ratio,
  shannonEntropy
} from './analysis'

describe('shannonEntropy', () => {
  it('is 0 when everything falls into a single bucket', () => {
    expect(shannonEntropy([10])).toBe(0)
    expect(shannonEntropy([10, 0, 0])).toBe(0)
  })

  it('equals log2(n) for an even split - the maximum diversity case', () => {
    expect(shannonEntropy([5, 5])).toBeCloseTo(1, 10)
    expect(shannonEntropy([1, 1, 1, 1])).toBeCloseTo(2, 10)
  })

  it('does not depend on scale, only on proportions', () => {
    expect(shannonEntropy([1, 3])).toBeCloseTo(shannonEntropy([100, 300]), 10)
  })

  it('returns 0 for an empty list instead of NaN', () => {
    expect(shannonEntropy([])).toBe(0)
    expect(shannonEntropy([0, 0])).toBe(0)
  })
})

describe('normalizedEntropy', () => {
  it('maps every table onto the same 0..1 scale regardless of row count', () => {
    expect(normalizedEntropy([5, 5])).toBeCloseTo(1, 10)
    expect(normalizedEntropy([1, 1, 1, 1, 1, 1, 1, 1])).toBeCloseTo(1, 10)
  })

  it('is 0 when only one bucket is non-zero', () => {
    expect(normalizedEntropy([7, 0, 0])).toBe(0)
    expect(normalizedEntropy([])).toBe(0)
  })

  it('sits between 0 and 1 for a skewed distribution', () => {
    const value = normalizedEntropy([100, 5, 3, 1])
    expect(value).toBeGreaterThan(0)
    expect(value).toBeLessThan(1)
  })
})

describe('headShare', () => {
  it('sums exactly the k largest buckets, whatever the input order', () => {
    expect(headShare([1, 50, 2, 30, 17], 2)).toBeCloseTo(0.8, 10)
  })

  it('is 1 when k exceeds the number of buckets', () => {
    expect(headShare([2, 3], 10)).toBe(1)
  })

  it('returns 0 when the total is 0', () => {
    expect(headShare([], 3)).toBe(0)
    expect(headShare([0, 0], 3)).toBe(0)
  })
})

describe('coverageRatio', () => {
  it('is the ratio of fetched pages to link targets seen', () => {
    expect(coverageRatio(250, 1000)).toBe(0.25)
  })

  it('caps at 1 - a corpus cannot cover more than 100%', () => {
    expect(coverageRatio(1200, 1000)).toBe(1)
  })

  it('returns 0 when no target has been discovered yet', () => {
    expect(coverageRatio(10, 0)).toBe(0)
  })
})

describe('ratio', () => {
  it('never returns NaN or Infinity', () => {
    expect(ratio(5, 0)).toBe(0)
    expect(ratio(Number.NaN, 10)).toBe(0)
    expect(ratio(3, 12)).toBe(0.25)
  })
})

describe('foldTail', () => {
  it('keeps the k largest entries and folds the rest together', () => {
    const folded = foldTail(
      [
        { label: 'vi', count: 80 },
        { label: 'en', count: 15 },
        { label: 'und', count: 3 },
        { label: 'fr', count: 1 },
        { label: 'de', count: 1 }
      ],
      3
    )

    expect(folded).toEqual([
      { label: 'vi', value: 80 },
      { label: 'en', value: 15 },
      { label: 'und', value: 3 },
      { label: 'other', value: 2 }
    ])
  })

  it('does not add an "other" row when there is no tail', () => {
    const folded = foldTail([{ label: 'vi', count: 5 }], 3)
    expect(folded).toEqual([{ label: 'vi', value: 5 }])
  })

  it('does not mutate the original array', () => {
    const input = [
      { label: 'a', count: 1 },
      { label: 'b', count: 9 }
    ]
    foldTail(input, 1)
    expect(input[0].label).toBe('a')
  })
})

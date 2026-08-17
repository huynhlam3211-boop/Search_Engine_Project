import { describe, expect, it } from 'vitest'
import { Stack } from './Stack'

describe('Stack', () => {
  it('pops in the reverse order of pushes (LIFO)', () => {
    const stack = new Stack<string>()
    stack.push('a')
    stack.push('b')
    stack.push('c')

    expect(stack.pop()).toBe('c')
    expect(stack.pop()).toBe('b')
    expect(stack.pop()).toBe('a')
  })

  it('returns undefined instead of throwing when empty', () => {
    const stack = new Stack<number>()
    expect(stack.pop()).toBeUndefined()
    expect(stack.peek()).toBeUndefined()
    expect(stack.isEmpty()).toBe(true)
    expect(stack.size()).toBe(0)
  })

  it('peek reads the top WITHOUT removing it', () => {
    const stack = new Stack<number>()
    stack.push(1)
    stack.push(2)

    expect(stack.peek()).toBe(2)
    expect(stack.size()).toBe(2)
    expect(stack.peek()).toBe(2)
  })

  it('clear empties the stack', () => {
    const stack = new Stack<number>()
    stack.push(1)
    stack.push(2)
    stack.clear()

    expect(stack.isEmpty()).toBe(true)
    expect(stack.toArray()).toEqual([])
  })

  it('toArray returns a COPY, not the internal array', () => {
    const stack = new Stack<number>()
    stack.push(1)

    const snapshot = stack.toArray()
    snapshot.push(999)

    expect(stack.size()).toBe(1)
    expect(stack.toArray()).toEqual([1])
  })

  it('keeps bottom to top order in toArray', () => {
    const stack = new Stack<string>()
    stack.push('bottom')
    stack.push('middle')
    stack.push('top')

    expect(stack.toArray()).toEqual(['bottom', 'middle', 'top'])
  })
})

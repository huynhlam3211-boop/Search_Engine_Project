import { useSyncExternalStore } from 'react'

/**
 * BAN VA TAM THOI — thay cho zustand.
 *
 * Cac store trong du an duoc viet theo API cua zustand (`create((set, get) => ...)`
 * roi dung bang selector). zustand chua co trong package.json nen o day tu cai
 * lai phan toi thieu bang useSyncExternalStore. Khi nao cai zustand that thi chi
 * can doi dong import trong cac file store, than store giu nguyen.
 *
 * LUU Y: selector phai tra ve gia tri nguyen thuy hoac tham chieu on dinh.
 * Tra ve object/array tao moi moi lan goi se lam React lap vo han.
 */

type SetState<T> = (partial: Partial<T> | ((state: T) => Partial<T>)) => void
type GetState<T> = () => T
type Creator<T> = (set: SetState<T>, get: GetState<T>) => T

export interface UseStore<T> {
  <U>(selector: (state: T) => U): U
  getState: GetState<T>
  setState: SetState<T>
  subscribe: (listener: () => void) => () => void
}

export function create<T extends object>(creator: Creator<T>): UseStore<T> {
  const listeners = new Set<() => void>()

  const setState: SetState<T> = (partial) => {
    const patch = typeof partial === 'function' ? partial(state) : partial
    state = { ...state, ...patch }
    listeners.forEach((listener) => listener())
  }

  const getState: GetState<T> = () => state

  const subscribe = (listener: () => void): (() => void) => {
    listeners.add(listener)
    return () => {
      listeners.delete(listener)
    }
  }

  let state: T = creator(setState, getState)

  const useStore = <U,>(selector: (state: T) => U): U =>
    useSyncExternalStore(
      subscribe,
      () => selector(state),
      () => selector(state)
    )

  return Object.assign(useStore, { getState, setState, subscribe })
}

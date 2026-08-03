import { useCallback, useEffect, useState } from 'react'

/**
 * Minimal async-resource hook. Avoids pulling in a query library while
 * still giving components: data, loading, error, and a refetch() escape hatch.
 */
export function useAsync(fn, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null })

  const run = useCallback(async () => {
    setState((s) => ({ ...s, loading: true, error: null }))
    try {
      const data = await fn()
      setState({ data, loading: false, error: null })
    } catch (error) {
      setState({ data: null, loading: false, error })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => {
    run()
  }, [run])

  return { ...state, refetch: run }
}

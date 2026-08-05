import { pick } from './format.js'

/**
 * Format date for CSV export (Excel-friendly format)
 */
function formatDateForCSV(value) {
  if (!value) return ''
  try {
    const date = new Date(value)
    if (isNaN(date.getTime())) return String(value)
    // Return in DD-MM-YYYY format (Excel-friendly)
    const day = String(date.getDate()).padStart(2, '0')
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const year = date.getFullYear()
    return `${day}-${month}-${year}`
  } catch {
    return String(value)
  }
}

/**
 * Format number for CSV export (clean decimal format)
 */
function formatNumberForCSV(value) {
  if (value === null || value === undefined || value === '') return ''
  const num = Number(value)
  if (isNaN(num)) return String(value)
  // Return number with up to 2 decimal places, remove trailing zeros
  return num.toFixed(2).replace(/\.?0+$/, '')
}

/**
 * Clean value for CSV (remove special characters, handle null/undefined)
 */
function cleanValueForCSV(value) {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

/**
 * Convert array of objects to CSV string
 */
function arrayToCSV(data, headers) {
  if (!data || data.length === 0) return ''

  // Create header row
  const headerRow = headers.map((h) => `"${h.label}"`).join(',')

  // Create data rows
  const dataRows = data.map((row) => {
    return headers
      .map((h) => {
        const value = h.getValue ? h.getValue(row) : row[h.key]
        const escaped = String(value ?? '').replace(/"/g, '""')
        return `"${escaped}"`
      })
      .join(',')
  })

  return [headerRow, ...dataRows].join('\n')
}

/**
 * Trigger CSV file download in browser
 */
function downloadCSV(csvContent, filename) {
  // Add UTF-8 BOM for better Excel compatibility
  const BOM = '\uFEFF'
  const csvWithBOM = BOM + csvContent

  const blob = new Blob([csvWithBOM], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)

  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.style.visibility = 'hidden'

  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  URL.revokeObjectURL(url)
}

/**
 * Export investments to CSV
 */
export function exportInvestmentsToCSV(investments, baseCurrency = 'INR') {
  const headers = [
    {
      key: 'symbol',
      label: 'Symbol',
      getValue: (inv) => cleanValueForCSV(pick(inv, ['symbol'], ''))
    },
    {
      key: 'name',
      label: 'Company Name',
      getValue: (inv) => cleanValueForCSV(pick(inv, ['name'], ''))
    },
    {
      key: 'type',
      label: 'Type',
      getValue: (inv) => cleanValueForCSV(pick(inv, ['type', 'investmentType'], ''))
    },
    {
      key: 'country',
      label: 'Country',
      getValue: (inv) => cleanValueForCSV(pick(inv, ['country', 'market'], ''))
    },
    {
      key: 'currency',
      label: 'Currency',
      getValue: (inv) => cleanValueForCSV(pick(inv, ['currency'], ''))
    },
    {
      key: 'quantity',
      label: 'Quantity',
      getValue: (inv) => formatNumberForCSV(pick(inv, ['quantity'], ''))
    },
    {
      key: 'avgBuyPrice',
      label: 'Purchase Price',
      getValue: (inv) => formatNumberForCSV(pick(inv, ['avgBuyPrice', 'purchasePrice'], ''))
    },
    {
      key: 'currentPrice',
      label: 'Current Price',
      getValue: (inv) => formatNumberForCSV(pick(inv, ['currentPrice', 'price'], ''))
    },
    {
      key: 'investedAmount',
      label: 'Invested Amount',
      getValue: (inv) => formatNumberForCSV(pick(inv, ['investedAmount', 'totalInvested'], 0))
    },
    {
      key: 'currentValue',
      label: 'Current Value',
      getValue: (inv) => formatNumberForCSV(pick(inv, ['currentValue', 'currentAmount'], 0))
    },
    {
      key: 'unrealizedPl',
      label: 'Profit/Loss',
      getValue: (inv) => {
        const invested = Number(pick(inv, ['investedAmount', 'totalInvested'], 0))
        const current = Number(pick(inv, ['currentValue', 'currentAmount'], 0))
        const pl = current - invested
        return formatNumberForCSV(pl)
      }
    },
    {
      key: 'unrealizedPlPercent',
      label: 'Profit/Loss %',
      getValue: (inv) => formatNumberForCSV(pick(inv, ['unrealizedPlPercent', 'gainLossPercentage', 'returnPercentage'], 0))
    },
    {
      key: 'investedAmountBase',
      label: `Invested (${baseCurrency})`,
      getValue: (inv) => formatNumberForCSV(pick(inv, ['investedAmountBase'], ''))
    },
    {
      key: 'currentValueBase',
      label: `Current Value (${baseCurrency})`,
      getValue: (inv) => formatNumberForCSV(pick(inv, ['currentValueBase'], ''))
    },
    {
      key: 'purchaseDate',
      label: 'Purchase Date',
      getValue: (inv) => formatDateForCSV(pick(inv, ['purchaseDate'], ''))
    },
    {
      key: 'status',
      label: 'Status',
      getValue: (inv) => cleanValueForCSV(pick(inv, ['status'], 'ACTIVE'))
    },
    {
      key: 'notes',
      label: 'Notes',
      getValue: (inv) => cleanValueForCSV(pick(inv, ['notes'], ''))
    },
  ]

  const csv = arrayToCSV(investments, headers)
  const filename = `portfolio_investments_${new Date().toISOString().slice(0, 10)}.csv`

  downloadCSV(csv, filename)
}

/**
 * Export transactions to CSV
 */
export function exportTransactionsToCSV(transactions, baseCurrency = 'INR') {
  const headers = [
    {
      key: 'date',
      label: 'Date',
      getValue: (tx) => formatDateForCSV(pick(tx, ['date', 'transactionDate', 'createdAt'], ''))
    },
    {
      key: 'type',
      label: 'Transaction Type',
      getValue: (tx) => cleanValueForCSV(pick(tx, ['type', 'transactionType'], ''))
    },
    {
      key: 'investment',
      label: 'Investment Name',
      getValue: (tx) => cleanValueForCSV(pick(tx, ['investmentName', 'investmentSymbol', 'name', 'symbol'], ''))
    },
    {
      key: 'quantity',
      label: 'Quantity',
      getValue: (tx) => formatNumberForCSV(pick(tx, ['quantity'], ''))
    },
    {
      key: 'price',
      label: 'Price per Unit',
      getValue: (tx) => formatNumberForCSV(pick(tx, ['price'], ''))
    },
    {
      key: 'amount',
      label: 'Total Amount',
      getValue: (tx) => formatNumberForCSV(pick(tx, ['amount', 'totalAmount', 'value'], 0))
    },
    {
      key: 'realizedPl',
      label: 'Realized P/L',
      getValue: (tx) => formatNumberForCSV(pick(tx, ['realizedPl', 'realizedProfitLoss'], ''))
    },
    {
      key: 'currency',
      label: 'Currency',
      getValue: (tx) => cleanValueForCSV(pick(tx, ['currency'], baseCurrency))
    },
    {
      key: 'notes',
      label: 'Notes',
      getValue: (tx) => cleanValueForCSV(pick(tx, ['notes'], ''))
    },
  ]

  const csv = arrayToCSV(transactions, headers)
  const filename = `portfolio_transactions_${new Date().toISOString().slice(0, 10)}.csv`

  downloadCSV(csv, filename)
}

/**
 * Export portfolio summary to CSV
 */
export function exportPortfolioSummaryToCSV(dashboard, investments, baseCurrency = 'INR') {
  const summaryData = [
    { metric: 'Total Invested', value: formatNumberForCSV(pick(dashboard, ['totalInvested', 'totalInvestedAmount'], 0)) },
    { metric: 'Current Value', value: formatNumberForCSV(pick(dashboard, ['totalCurrentValue', 'currentValue'], 0)) },
    { metric: 'Unrealized P/L', value: formatNumberForCSV(pick(dashboard, ['totalUnrealizedPl', 'unrealizedProfitLoss'], 0)) },
    { metric: 'Unrealized P/L %', value: formatNumberForCSV(pick(dashboard, ['totalUnrealizedPlPercent', 'returnPercentage'], 0)) },
    { metric: 'Total Gain/Loss Today', value: formatNumberForCSV(pick(dashboard, ['todayGainLoss', 'dailyChange'], 0)) },
    { metric: 'Total Gain/Loss Today %', value: formatNumberForCSV(pick(dashboard, ['todayGainLossPercent', 'dailyChangePercent'], 0)) },
    { metric: 'Total Investments', value: investments?.length || 0 },
    { metric: 'Base Currency', value: baseCurrency },
    { metric: 'Report Date', value: formatDateForCSV(new Date()) },
  ]

  const headers = [
    { key: 'metric', label: 'Metric' },
    { key: 'value', label: 'Value' },
  ]

  let csv = '=== PORTFOLIO SUMMARY ===\n\n'
  csv += arrayToCSV(summaryData, headers)

  // Add investment breakdown
  if (investments && investments.length > 0) {
    csv += '\n\n=== INVESTMENTS BREAKDOWN ===\n\n'
    const invHeaders = [
      {
        key: 'name',
        label: 'Name',
        getValue: (inv) => cleanValueForCSV(pick(inv, ['name', 'symbol'], 'Unnamed'))
      },
      {
        key: 'type',
        label: 'Type',
        getValue: (inv) => cleanValueForCSV(pick(inv, ['type'], ''))
      },
      {
        key: 'invested',
        label: 'Invested',
        getValue: (inv) => formatNumberForCSV(pick(inv, ['investedAmount'], 0))
      },
      {
        key: 'current',
        label: 'Current',
        getValue: (inv) => formatNumberForCSV(pick(inv, ['currentValue'], 0))
      },
      {
        key: 'pl',
        label: 'P/L',
        getValue: (inv) => {
          const invested = Number(pick(inv, ['investedAmount'], 0))
          const current = Number(pick(inv, ['currentValue'], 0))
          return formatNumberForCSV(current - invested)
        }
      },
      {
        key: 'plPercent',
        label: 'P/L %',
        getValue: (inv) => formatNumberForCSV(pick(inv, ['unrealizedPlPercent'], 0))
      },
    ]
    csv += arrayToCSV(investments, invHeaders)
  }

  const filename = `portfolio_summary_${new Date().toISOString().slice(0, 10)}.csv`
  downloadCSV(csv, filename)
}







import { useMemo, useState } from "react";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

import { Card, SectionHeading, Skeleton, EmptyState } from '../ui.jsx'

const ALLOC_COLORS = [
  "#E649A1",
  "#B48CFF",
  "#F39BC7",
  "#8E63F4",
  "#F06292",
  "#DEC9FF",
  "#D946EF",
  "#C92E86",
];

const TABS = [
  { key: "type", label: "Asset Type" },
  { key: "country", label: "Country" },
  { key: "currency", label: "Currency" },
];

function formatCurrency(value, currency = "INR") {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    maximumFractionDigits: 0,
  }).format(value || 0);
}

function AllocationCard({
  allocations = {},
  baseCurrency = "INR",
  loading,
}) {
  const [activeTab, setActiveTab] = useState("type");

  const data = useMemo(() => {
    const list = allocations?.[activeTab] || [];

    return list.map((item) => ({
      name: item.label,
      value: Number(item.percentage),
      amount: Number(item.valueBase),
    }));
  }, [allocations, activeTab]);

  if (loading) {
    return (
      <Card className="p-6">
        <Skeleton className="h-72 w-full rounded-xl" />
      </Card>
    );
  }

  return (
    <Card className="p-6">
      <SectionHeading
        eyebrow="Diversification"
        title="Portfolio Allocation"
      />

      {/* Tabs */}

      <div className="mb-6 mt-6 flex rounded-xl border border-line bg-paper-sunken/80 p-1">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 rounded-lg py-2 text-sm font-medium transition-all
            ${
              activeTab === tab.key
                ? "border border-rose/35 bg-paper-raised text-rose-deep shadow-glass-sm dark:text-rose-soft"
                : "text-ink-soft hover:bg-paper-raised hover:text-ink"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {data.length === 0 ? (
        <EmptyState
          title="No allocation data"
          description="Add investments to see portfolio diversification."
        />
      ) : (
        <div className="flex flex-col gap-6 lg:flex-row lg:items-center">
          {/* Chart */}

          <div className="mx-auto h-72 w-72">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={data}
                  innerRadius={70}
                  outerRadius={100}
                  paddingAngle={3}
                  dataKey="value"
                  nameKey="name"
                >
                  {data.map((_, index) => (
                    <Cell
                      key={index}
                      fill={ALLOC_COLORS[index % ALLOC_COLORS.length]}
                    />
                  ))}
                </Pie>

                <Tooltip
                  formatter={(value, name, props) => [
                    formatCurrency(props.payload.amount, baseCurrency),
                    `${name} (${value}%)`,
                  ]}
                  contentStyle={{
                    borderRadius: 12,
                    border: "1px solid rgb(var(--line) / 0.8)",
                    backgroundColor: "rgb(var(--paper-raised))",
                    color: "rgb(var(--ink))",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>

            <div className="-mt-44 flex flex-col items-center justify-center">
              <p className="text-sm text-ink-faint">
                {TABS.find((t) => t.key === activeTab)?.label}
              </p>

              <p className="text-3xl font-bold text-ink">
                {data.length}
              </p>

              <p className="text-xs text-ink-faint">
                Categories
              </p>
            </div>
          </div>

          {/* Legend */}

          <div className="flex-1 space-y-4">
            {data.map((item, index) => (
              <div
                key={item.name}
                className="flex items-center justify-between rounded-xl border border-line bg-paper-raised/70 p-3"
              >
                <div className="flex items-center gap-3">
                  <span
                    className="h-3 w-3 rounded-full"
                    style={{
                      background:
                        ALLOC_COLORS[index % ALLOC_COLORS.length],
                    }}
                  />

                  <div>
                    <p className="font-medium">{item.name}</p>

                    <p className="text-sm text-ink-faint">
                      {formatCurrency(item.amount, baseCurrency)}
                    </p>
                  </div>
                </div>

                <span className="font-semibold text-rose-deep dark:text-rose-soft">
                  {item.value.toFixed(2)}%
                </span>
              </div>
            ))}

            <div className="mt-4 rounded-xl border border-line bg-gradient-to-r from-rose/10 to-violet/10 p-4">
              <p className="text-sm text-ink-faint">
                Base Currency
              </p>

              <p className="text-lg font-semibold text-ink">
                {baseCurrency}
              </p>
            </div>
          </div>
        </div>
      )}
    </Card>
  );
}

export default AllocationCard;

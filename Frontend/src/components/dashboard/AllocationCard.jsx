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
  "#2563EB",
  "#22C55E",
  "#F59E0B",
  "#EF4444",
  "#8B5CF6",
  "#06B6D4",
  "#84CC16",
  "#F97316",
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

      <div className="mt-6 mb-6 flex rounded-xl bg-muted p-1">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex-1 rounded-lg py-2 text-sm font-medium transition-all
            ${
              activeTab === tab.key
                ? "bg-primary text-primary-foreground shadow"
                : "text-muted-foreground hover:bg-background"
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
                    border: "1px solid #E5E7EB",
                    backgroundColor: "#fff",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>

            <div className="-mt-44 flex flex-col items-center justify-center">
              <p className="text-sm text-muted-foreground">
                {TABS.find((t) => t.key === activeTab)?.label}
              </p>

              <p className="text-3xl font-bold">
                {data.length}
              </p>

              <p className="text-xs text-muted-foreground">
                Categories
              </p>
            </div>
          </div>

          {/* Legend */}

          <div className="flex-1 space-y-4">
            {data.map((item, index) => (
              <div
                key={item.name}
                className="flex items-center justify-between rounded-lg border p-3"
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

                    <p className="text-sm text-muted-foreground">
                      {formatCurrency(item.amount, baseCurrency)}
                    </p>
                  </div>
                </div>

                <span className="font-semibold">
                  {item.value.toFixed(2)}%
                </span>
              </div>
            ))}

            <div className="mt-4 rounded-lg bg-muted p-4">
              <p className="text-sm text-muted-foreground">
                Base Currency
              </p>

              <p className="text-lg font-semibold">
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
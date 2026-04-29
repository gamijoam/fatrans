'use client';

import { useState } from 'react';
import { ChevronLeft, ChevronRight, Search, Filter, MoreHorizontal, ArrowUpDown, ArrowUp, ArrowDown, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';

interface Column<T> {
  key: string;
  label: string;
  sortable?: boolean;
  width?: string;
  render?: (item: T) => React.ReactNode;
}

interface AdvancedTableProps<T> {
  columns: Column<T>[];
  data: T[];
  actions?: {
    label: string;
    icon?: React.ElementType;
    onClick: (item: T) => void;
    variant?: 'default' | 'outline' | 'ghost' | 'destructive';
  }[];
  searchable?: boolean;
  searchPlaceholder?: string;
  filterable?: boolean;
  filters?: { key: string; label: string; options: { value: string; label: string }[] }[];
  onFilterChange?: (filters: Record<string, string>) => void;
  pageSize?: number;
  emptyMessage?: string;
  onRowClick?: (item: T) => void;
}

export function AdvancedTable<T extends { id: string | number }>({
  columns,
  data,
  actions,
  searchable = true,
  searchPlaceholder = 'Buscar...',
  filterable = true,
  filters = [],
  onFilterChange,
  pageSize = 10,
  emptyMessage = 'No hay datos disponibles',
  onRowClick,
}: AdvancedTableProps<T>) {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilters, setActiveFilters] = useState<Record<string, string>>({});
  const [sortKey, setSortKey] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [currentPage, setCurrentPage] = useState(1);
  const [showFilters, setShowFilters] = useState(false);

  const handleSort = (key: string) => {
    if (sortKey === key) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortKey(key);
      setSortDirection('asc');
    }
  };

  const handleFilterChange = (key: string, value: string) => {
    const newFilters = { ...activeFilters, [key]: value };
    setActiveFilters(newFilters);
    onFilterChange?.(newFilters);
    setCurrentPage(1);
  };

  const clearFilters = () => {
    setActiveFilters({});
    onFilterChange?.({});
    setCurrentPage(1);
  };

  const filteredData = data.filter((item) => {
    const matchesSearch = searchQuery === '' || Object.values(item).some((val) =>
      String(val).toLowerCase().includes(searchQuery.toLowerCase())
    );

    const matchesFilters = Object.entries(activeFilters).every(([key, value]) =>
      value === '' || String(item[key as keyof T]).toLowerCase() === value.toLowerCase()
    );

    return matchesSearch && matchesFilters;
  });

  const sortedData = [...filteredData].sort((a, b) => {
    if (!sortKey) return 0;
    const aVal = a[sortKey as keyof T];
    const bVal = b[sortKey as keyof T];
    const comparison = String(aVal).localeCompare(String(bVal));
    return sortDirection === 'asc' ? comparison : -comparison;
  });

  const totalPages = Math.ceil(sortedData.length / pageSize);
  const paginatedData = sortedData.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  const activeFilterCount = Object.values(activeFilters).filter((v) => v !== '').length;

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        {/* Search */}
        {searchable && (
          <div className="relative w-full sm:w-80">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <Input
              placeholder={searchPlaceholder}
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setCurrentPage(1);
              }}
              className="pl-10 h-10 bg-slate-50 border-slate-200 focus:bg-white"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>
        )}

        <div className="flex items-center gap-2 w-full sm:w-auto">
          {/* Filters Toggle */}
          {filterable && filters.length > 0 && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowFilters(!showFilters)}
              className="h-10"
            >
              <Filter className="w-4 h-4 mr-2" />
              Filtros
              {activeFilterCount > 0 && (
                <Badge className="ml-2 bg-[#16A34A] text-white text-[10px] px-1.5 py-0.5 rounded-full">
                  {activeFilterCount}
                </Badge>
              )}
            </Button>
          )}

          {/* Clear Filters */}
          {activeFilterCount > 0 && (
            <Button variant="ghost" size="sm" onClick={clearFilters} className="h-10 text-slate-500">
              Limpiar
            </Button>
          )}
        </div>
      </div>

      {/* Filter Bar */}
      {showFilters && filterable && filters.length > 0 && (
        <div className="flex flex-wrap items-center gap-3 p-4 bg-slate-50 rounded-xl border border-slate-200">
          {filters.map((filter) => (
            <div key={filter.key} className="space-y-1">
              <label className="text-xs font-medium text-slate-500 uppercase tracking-wide">
                {filter.label}
              </label>
              <select
                value={activeFilters[filter.key] || ''}
                onChange={(e) => handleFilterChange(filter.key, e.target.value)}
                className="h-9 px-3 pr-8 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#16A34A]/20 focus:border-[#16A34A]"
              >
                <option value="">Todos</option>
                {filter.options.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          ))}
        </div>
      )}

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                {columns.map((col) => (
                  <th
                    key={col.key}
                    className={`px-5 py-3.5 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide ${
                      col.sortable ? 'cursor-pointer hover:text-slate-700 select-none' : ''
                    }`}
                    style={{ width: col.width }}
                    onClick={() => col.sortable && handleSort(col.key)}
                  >
                    <div className="flex items-center gap-2">
                      {col.label}
                      {col.sortable && (
                        <span className="text-slate-300">
                          {sortKey === col.key ? (
                            sortDirection === 'asc' ? (
                              <ArrowUp className="w-3.5 h-3.5" />
                            ) : (
                              <ArrowDown className="w-3.5 h-3.5" />
                            )
                          ) : (
                            <ArrowUpDown className="w-3.5 h-3.5" />
                          )}
                        </span>
                      )}
                    </div>
                  </th>
                ))}
                {actions && actions.length > 0 && (
                  <th className="px-5 py-3.5 text-right text-xs font-semibold text-slate-500 uppercase tracking-wide">
                    Acciones
                  </th>
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {paginatedData.length === 0 ? (
                <tr>
                  <td colSpan={columns.length + (actions ? 1 : 0)} className="px-5 py-12 text-center">
                    <div className="flex flex-col items-center text-slate-500">
                      <Search className="w-10 h-10 mb-3 text-slate-300" />
                      <p className="text-sm">{emptyMessage}</p>
                    </div>
                  </td>
                </tr>
              ) : (
                paginatedData.map((item) => (
                  <tr
                    key={item.id}
                    className={`hover:bg-slate-50 transition-colors ${onRowClick ? 'cursor-pointer' : ''}`}
                    onClick={() => onRowClick?.(item)}
                  >
                    {columns.map((col) => (
                      <td key={col.key} className="px-5 py-4">
                        {col.render ? col.render(item) : (
                          <span className="text-sm text-slate-600">
                            {String(item[col.key as keyof T])}
                          </span>
                        )}
                      </td>
                    ))}
                    {actions && actions.length > 0 && (
                      <td className="px-5 py-4 text-right" onClick={(e) => e.stopPropagation()}>
                        <div className="flex items-center justify-end gap-1">
                          {actions.length === 1 ? (
                            <button
                              onClick={() => actions[0].onClick(item)}
                              className={`p-1.5 rounded-lg transition-colors ${
                                actions[0].variant === 'destructive'
                                  ? 'hover:bg-red-100 text-red-600'
                                  : 'hover:bg-slate-200 text-slate-500'
                              }`}
                            >
                              {actions[0].icon && (() => {
                                const Icon = actions[0].icon;
                                return <Icon className="w-4 h-4" />;
                              })()}
                            </button>
                          ) : (
                            <div className="relative group">
                              <button className="p-1.5 hover:bg-slate-200 rounded-lg transition-colors">
                                <MoreHorizontal className="w-4 h-4 text-slate-500" />
                              </button>
                              <div className="absolute right-0 mt-1 w-48 bg-white rounded-xl shadow-xl border border-slate-200 py-1 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all z-10">
                                {actions.map((action, idx) => (
                                  <button
                                    key={idx}
                                    onClick={() => action.onClick(item)}
                                    className={`w-full px-4 py-2 text-left text-sm flex items-center gap-2 transition-colors ${
                                      action.variant === 'destructive'
                                        ? 'text-red-600 hover:bg-red-50'
                                        : 'text-slate-700 hover:bg-slate-50'
                                    }`}
                                  >
                                    {action.icon && <action.icon className="w-4 h-4" />}
                                    {action.label}
                                  </button>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-5 py-4 border-t border-slate-200 bg-slate-50">
            <p className="text-xs text-slate-500">
              Mostrando {((currentPage - 1) * pageSize) + 1} - {Math.min(currentPage * pageSize, sortedData.length)} de {sortedData.length}
            </p>
            <div className="flex items-center gap-1">
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(currentPage - 1)}
              >
                <ChevronLeft className="w-4 h-4" />
              </Button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                let pageNum: number;
                if (totalPages <= 5) {
                  pageNum = i + 1;
                } else if (currentPage <= 3) {
                  pageNum = i + 1;
                } else if (currentPage >= totalPages - 2) {
                  pageNum = totalPages - 4 + i;
                } else {
                  pageNum = currentPage - 2 + i;
                }
                return (
                  <Button
                    key={pageNum}
                    variant={currentPage === pageNum ? 'default' : 'outline'}
                    size="icon"
                    className={`h-8 w-8 text-xs ${currentPage === pageNum ? 'bg-[#16A34A] hover:bg-[#15803D]' : ''}`}
                    onClick={() => setCurrentPage(pageNum)}
                  >
                    {pageNum}
                  </Button>
                );
              })}
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(currentPage + 1)}
              >
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

import { useState } from 'react';
import { BookOpen, RotateCcw, Calendar, AlertCircle, CheckCircle2 } from 'lucide-react';

type TransactionCardProps = {
  onBorrow: (data: { readerId: string; isbn: string; dueDate?: string }) => void;
  onReturn: (data: { isbn: string; readerId?: string }) => void;
  borrowStatus?: { type: 'success' | 'error'; message: string } | null;
  returnStatus?: { type: 'success' | 'error'; message: string; overdueDays?: number; fine?: number } | null;
};

export function TransactionCard({ onBorrow, onReturn, borrowStatus, returnStatus }: TransactionCardProps) {
  const [activeTab, setActiveTab] = useState<'borrow' | 'return'>('borrow');
  const [borrowData, setBorrowData] = useState({ readerId: '', isbn: '', dueDate: '' });
  const [returnData, setReturnData] = useState({ isbn: '', readerId: '' });

  const handleBorrowSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (borrowData.readerId && borrowData.isbn) {
      onBorrow({
        readerId: borrowData.readerId,
        isbn: borrowData.isbn,
        dueDate: borrowData.dueDate || undefined,
      });
    }
  };

  const handleReturnSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (returnData.isbn) {
      onReturn({
        isbn: returnData.isbn,
        readerId: returnData.readerId || undefined,
      });
    }
  };

  return (
    <div className="rounded-lg border bg-white shadow-sm">
      {/* Tabs */}
      <div className="border-b">
        <div className="flex">
          <button
            onClick={() => setActiveTab('borrow')}
            className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 text-sm font-medium transition-colors ${
              activeTab === 'borrow'
                ? 'border-b-2 border-blue-600 text-blue-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            <BookOpen className="h-4 w-4" />
            借書
          </button>
          <button
            onClick={() => setActiveTab('return')}
            className={`flex-1 flex items-center justify-center gap-2 px-4 py-3 text-sm font-medium transition-colors ${
              activeTab === 'return'
                ? 'border-b-2 border-green-600 text-green-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            <RotateCcw className="h-4 w-4" />
            還書
          </button>
        </div>
      </div>

      <div className="p-6">
        {activeTab === 'borrow' ? (
          <form onSubmit={handleBorrowSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                讀者 ID <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={borrowData.readerId}
                onChange={(e) => setBorrowData({ ...borrowData, readerId: e.target.value })}
                placeholder="請輸入讀者 ID"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                ISBN <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={borrowData.isbn}
                onChange={(e) => setBorrowData({ ...borrowData, isbn: e.target.value })}
                placeholder="請輸入 ISBN"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1 flex items-center gap-1">
                <Calendar className="h-3.5 w-3.5" />
                到期日（可選）
              </label>
              <input
                type="date"
                value={borrowData.dueDate}
                onChange={(e) => setBorrowData({ ...borrowData, dueDate: e.target.value })}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
              />
            </div>

            {borrowStatus && (
              <div
                className={`flex items-start gap-2 rounded-md p-3 text-sm ${
                  borrowStatus.type === 'success'
                    ? 'bg-green-50 text-green-800'
                    : 'bg-red-50 text-red-800'
                }`}
              >
                {borrowStatus.type === 'success' ? (
                  <CheckCircle2 className="h-4 w-4 mt-0.5 flex-shrink-0" />
                ) : (
                  <AlertCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
                )}
                <span>{borrowStatus.message}</span>
              </div>
            )}

            <button
              type="submit"
              className="w-full rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              確認借出
            </button>
          </form>
        ) : (
          <form onSubmit={handleReturnSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                ISBN <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={returnData.isbn}
                onChange={(e) => setReturnData({ ...returnData, isbn: e.target.value })}
                placeholder="請輸入 ISBN"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-green-500 focus:ring-2 focus:ring-green-100"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                讀者 ID（可選）
              </label>
              <input
                type="text"
                value={returnData.readerId}
                onChange={(e) => setReturnData({ ...returnData, readerId: e.target.value })}
                placeholder="請輸入讀者 ID（可選）"
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-green-500 focus:ring-2 focus:ring-green-100"
              />
            </div>

            {returnStatus && (
              <div
                className={`rounded-md p-3 text-sm ${
                  returnStatus.type === 'success'
                    ? 'bg-green-50 text-green-800'
                    : 'bg-amber-50 text-amber-800'
                }`}
              >
                <div className="flex items-start gap-2">
                  {returnStatus.type === 'success' ? (
                    <CheckCircle2 className="h-4 w-4 mt-0.5 flex-shrink-0" />
                  ) : (
                    <AlertCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
                  )}
                  <div className="flex-1">
                    <p className="font-medium">{returnStatus.message}</p>
                    {returnStatus.overdueDays !== undefined && returnStatus.overdueDays > 0 && (
                      <div className="mt-2 space-y-1">
                        <p className="text-xs">逾期天數: {returnStatus.overdueDays} 天</p>
                        <p className="text-xs">罰款金額: NT$ {returnStatus.fine}</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}

            <button
              type="submit"
              className="w-full rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2"
            >
              確認歸還
            </button>
          </form>
        )}
      </div>
    </div>
  );
}

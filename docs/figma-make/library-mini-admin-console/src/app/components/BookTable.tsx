import { BookOpen, RotateCcw, BookX } from 'lucide-react';

type Book = {
  id: string;
  title: string;
  isbn: string;
  author?: string;
  category: string;
  status: 'available' | 'borrowed' | 'inactive';
  availableCount: number;
  totalCount: number;
  borrowedBy?: string;
  dueDate?: string;
};

type BookTableProps = {
  books: Book[];
  onQuickBorrow: (isbn: string) => void;
  onQuickReturn: (isbn: string) => void;
};

export function BookTable({ books, onQuickBorrow, onQuickReturn }: BookTableProps) {
  const getCategoryLabel = (category: string) => {
    const labels: Record<string, string> = {
      literature: '文學',
      science: '科學',
      technology: '科技',
      history: '歷史',
      art: '藝術',
      philosophy: '哲學',
      business: '商業',
      education: '教育',
    };
    return labels[category] || category;
  };

  const getStatusBadge = (status: string) => {
    const badges: Record<string, { label: string; className: string }> = {
      available: {
        label: '可借閱',
        className: 'bg-green-100 text-green-800',
      },
      borrowed: {
        label: '已借出',
        className: 'bg-blue-100 text-blue-800',
      },
      inactive: {
        label: '未上架',
        className: 'bg-gray-100 text-gray-800',
      },
    };
    const badge = badges[status] || badges.available;
    return (
      <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${badge.className}`}>
        {badge.label}
      </span>
    );
  };

  if (books.length === 0) {
    return (
      <div className="rounded-lg border bg-white shadow-sm">
        <div className="border-b px-6 py-4">
          <h2 className="font-semibold text-gray-900">館藏列表</h2>
        </div>
        <div className="flex flex-col items-center justify-center py-16 px-6 text-center">
          <div className="rounded-full bg-gray-100 p-4 mb-4">
            <BookX className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="text-sm font-medium text-gray-900 mb-1">尚無館藏</h3>
          <p className="text-sm text-gray-500">請使用右側表單新增書籍</p>
        </div>
      </div>
    );
  }

  return (
    <div className="rounded-lg border bg-white shadow-sm">
      <div className="border-b px-6 py-4 flex items-center justify-between">
        <h2 className="font-semibold text-gray-900">館藏列表</h2>
        <span className="text-sm text-gray-500">共 {books.length} 本</span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                書名
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                ISBN
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                作者
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                分類
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                狀態
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                可借數/總數
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                動作
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {books.map((book) => (
              <tr key={book.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900">{book.title}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-700 font-mono">{book.isbn}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-700">{book.author || '-'}</div>
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-700">{getCategoryLabel(book.category)}</div>
                </td>
                <td className="px-6 py-4">
                  {getStatusBadge(book.status)}
                </td>
                <td className="px-6 py-4">
                  <div className="text-sm text-gray-900">
                    <span className={book.availableCount === 0 ? 'text-red-600' : 'text-green-600 font-medium'}>
                      {book.availableCount}
                    </span>
                    {' / '}
                    {book.totalCount}
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => onQuickBorrow(book.isbn)}
                      disabled={book.availableCount === 0 || book.status === 'inactive'}
                      className="inline-flex items-center gap-1.5 rounded-md bg-blue-50 px-3 py-1.5 text-xs font-medium text-blue-700 transition-colors hover:bg-blue-100 disabled:opacity-50 disabled:cursor-not-allowed"
                      title="快速借出"
                    >
                      <BookOpen className="h-3.5 w-3.5" />
                      借出
                    </button>
                    <button
                      onClick={() => onQuickReturn(book.isbn)}
                      disabled={book.availableCount === book.totalCount}
                      className="inline-flex items-center gap-1.5 rounded-md bg-green-50 px-3 py-1.5 text-xs font-medium text-green-700 transition-colors hover:bg-green-100 disabled:opacity-50 disabled:cursor-not-allowed"
                      title="快速歸還"
                    >
                      <RotateCcw className="h-3.5 w-3.5" />
                      歸還
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

import { useState } from 'react';
import { BookPlus, CheckCircle2, AlertCircle } from 'lucide-react';

type AddBookFormProps = {
  onAddBook: (data: {
    title: string;
    isbn: string;
    author?: string;
    category: string;
    quantity: number;
    isActive: boolean;
  }) => void;
  status?: { type: 'success' | 'error'; message: string } | null;
};

export function AddBookForm({ onAddBook, status }: AddBookFormProps) {
  const [formData, setFormData] = useState({
    title: '',
    isbn: '',
    author: '',
    category: 'literature',
    quantity: 1,
    isActive: true,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.title && formData.isbn) {
      onAddBook({
        title: formData.title,
        isbn: formData.isbn,
        author: formData.author || undefined,
        category: formData.category,
        quantity: formData.quantity,
        isActive: formData.isActive,
      });
    }
  };

  return (
    <div className="rounded-lg border bg-white shadow-sm">
      <div className="border-b px-6 py-4">
        <div className="flex items-center gap-2">
          <BookPlus className="h-5 w-5 text-indigo-600" />
          <h2 className="font-semibold text-gray-900">新增書籍</h2>
        </div>
      </div>

      <div className="p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              書名 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              placeholder="請輸入書名"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              ISBN <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.isbn}
              onChange={(e) => setFormData({ ...formData, isbn: e.target.value })}
              placeholder="請輸入 ISBN"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              作者（可選）
            </label>
            <input
              type="text"
              value={formData.author}
              onChange={(e) => setFormData({ ...formData, author: e.target.value })}
              placeholder="請輸入作者"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              分類 <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
            >
              <option value="literature">文學</option>
              <option value="science">科學</option>
              <option value="technology">科技</option>
              <option value="history">歷史</option>
              <option value="art">藝術</option>
              <option value="philosophy">哲學</option>
              <option value="business">商業</option>
              <option value="education">教育</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              數量 <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              min="1"
              value={formData.quantity}
              onChange={(e) => setFormData({ ...formData, quantity: parseInt(e.target.value) || 1 })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm outline-none transition-colors focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              required
            />
          </div>

          <div className="flex items-center justify-between rounded-md border border-gray-200 bg-gray-50 px-4 py-3">
            <div>
              <label htmlFor="isActive" className="text-sm font-medium text-gray-700">
                上架狀態
              </label>
              <p className="text-xs text-gray-500">書籍是否可供借閱</p>
            </div>
            <button
              type="button"
              id="isActive"
              role="switch"
              aria-checked={formData.isActive}
              onClick={() => setFormData({ ...formData, isActive: !formData.isActive })}
              className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                formData.isActive ? 'bg-indigo-600' : 'bg-gray-300'
              }`}
            >
              <span
                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                  formData.isActive ? 'translate-x-6' : 'translate-x-1'
                }`}
              />
            </button>
          </div>

          {status && (
            <div
              className={`flex items-start gap-2 rounded-md p-3 text-sm ${
                status.type === 'success'
                  ? 'bg-green-50 text-green-800'
                  : 'bg-red-50 text-red-800'
              }`}
            >
              {status.type === 'success' ? (
                <CheckCircle2 className="h-4 w-4 mt-0.5 flex-shrink-0" />
              ) : (
                <AlertCircle className="h-4 w-4 mt-0.5 flex-shrink-0" />
              )}
              <span>{status.message}</span>
            </div>
          )}

          <button
            type="submit"
            className="w-full rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
          >
            新增書籍
          </button>
        </form>
      </div>
    </div>
  );
}

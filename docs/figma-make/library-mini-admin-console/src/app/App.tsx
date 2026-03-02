import { useState } from 'react';
import { Toaster, toast } from 'sonner';
import { TopBar } from './components/TopBar';
import { TransactionCard } from './components/TransactionCard';
import { AddBookForm } from './components/AddBookForm';
import { BookTable } from './components/BookTable';

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

// Mock initial data
const initialBooks: Book[] = [
  {
    id: '1',
    title: '追風箏的孩子',
    isbn: '978-986-213-999-1',
    author: '卡勒德·胡賽尼',
    category: 'literature',
    status: 'available',
    availableCount: 3,
    totalCount: 3,
  },
  {
    id: '2',
    title: '深度學習',
    isbn: '978-7-115-48570-5',
    author: 'Ian Goodfellow',
    category: 'technology',
    status: 'borrowed',
    availableCount: 0,
    totalCount: 2,
  },
  {
    id: '3',
    title: '人類大歷史',
    isbn: '978-986-320-175-5',
    author: '哈拉瑞',
    category: 'history',
    status: 'available',
    availableCount: 2,
    totalCount: 4,
  },
  {
    id: '4',
    title: 'Clean Code',
    isbn: '978-0-13-235088-4',
    author: 'Robert C. Martin',
    category: 'technology',
    status: 'available',
    availableCount: 1,
    totalCount: 2,
  },
];

export default function App() {
  const [books, setBooks] = useState<Book[]>(initialBooks);
  const [borrowStatus, setBorrowStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [returnStatus, setReturnStatus] = useState<{
    type: 'success' | 'error';
    message: string;
    overdueDays?: number;
    fine?: number;
  } | null>(null);
  const [addBookStatus, setAddBookStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const handleBorrow = (data: { readerId: string; isbn: string; dueDate?: string }) => {
    const bookIndex = books.findIndex((b) => b.isbn === data.isbn);
    if (bookIndex === -1) {
      setBorrowStatus({ type: 'error', message: '找不到該 ISBN 的書籍' });
      toast.error('借書失敗：找不到該書籍');
      return;
    }

    const book = books[bookIndex];
    if (book.availableCount === 0) {
      setBorrowStatus({ type: 'error', message: '該書籍已全數借出' });
      toast.error('借書失敗：書籍已全數借出');
      return;
    }

    if (book.status === 'inactive') {
      setBorrowStatus({ type: 'error', message: '該書籍未上架，無法借閱' });
      toast.error('借書失敗：書籍未上架');
      return;
    }

    const dueDate = data.dueDate || new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

    const updatedBooks = [...books];
    updatedBooks[bookIndex] = {
      ...book,
      availableCount: book.availableCount - 1,
      status: book.availableCount - 1 === 0 ? 'borrowed' : book.status,
      borrowedBy: data.readerId,
      dueDate: dueDate,
    };

    setBooks(updatedBooks);
    setBorrowStatus({ type: 'success', message: `借書成功！到期日: ${dueDate}` });
    toast.success(`《${book.title}》借出成功`, {
      description: `讀者: ${data.readerId} | 到期: ${dueDate}`,
    });

    setTimeout(() => setBorrowStatus(null), 5000);
  };

  const handleReturn = (data: { isbn: string; readerId?: string }) => {
    const bookIndex = books.findIndex((b) => b.isbn === data.isbn);
    if (bookIndex === -1) {
      setReturnStatus({ type: 'error', message: '找不到該 ISBN 的書籍' });
      toast.error('還書失敗：找不到該書籍');
      return;
    }

    const book = books[bookIndex];
    if (book.availableCount === book.totalCount) {
      setReturnStatus({ type: 'error', message: '該書籍沒有借出記錄' });
      toast.error('還書失敗：沒有借出記錄');
      return;
    }

    // Check if overdue
    const today = new Date();
    const dueDate = book.dueDate ? new Date(book.dueDate) : today;
    const overdueDays = Math.max(0, Math.floor((today.getTime() - dueDate.getTime()) / (1000 * 60 * 60 * 24)));
    const fine = overdueDays * 5; // NT$5 per day

    const updatedBooks = [...books];
    updatedBooks[bookIndex] = {
      ...book,
      availableCount: book.availableCount + 1,
      status: book.availableCount + 1 === book.totalCount ? 'available' : book.status,
      borrowedBy: undefined,
      dueDate: undefined,
    };

    setBooks(updatedBooks);

    if (overdueDays > 0) {
      setReturnStatus({
        type: 'error',
        message: `還書成功，但書籍已逾期`,
        overdueDays,
        fine,
      });
      toast.warning(`《${book.title}》已歸還`, {
        description: `逾期 ${overdueDays} 天，罰款 NT$${fine}`,
      });
    } else {
      setReturnStatus({ type: 'success', message: '還書成功！' });
      toast.success(`《${book.title}》已歸還`, {
        description: data.readerId ? `讀者: ${data.readerId}` : '歸還完成',
      });
    }

    setTimeout(() => setReturnStatus(null), 5000);
  };

  const handleAddBook = (data: {
    title: string;
    isbn: string;
    author?: string;
    category: string;
    quantity: number;
    isActive: boolean;
  }) => {
    const existingBook = books.find((b) => b.isbn === data.isbn);
    if (existingBook) {
      setAddBookStatus({ type: 'error', message: 'ISBN 已存在，無法重複新增' });
      toast.error('新增失敗：ISBN 已存在');
      return;
    }

    const newBook: Book = {
      id: Date.now().toString(),
      title: data.title,
      isbn: data.isbn,
      author: data.author,
      category: data.category,
      status: data.isActive ? 'available' : 'inactive',
      availableCount: data.quantity,
      totalCount: data.quantity,
    };

    setBooks([...books, newBook]);
    setAddBookStatus({ type: 'success', message: '書籍新增成功！' });
    toast.success(`《${data.title}》已新增`, {
      description: `數量: ${data.quantity} 本`,
    });

    setTimeout(() => setAddBookStatus(null), 5000);
  };

  const handleQuickBorrow = (isbn: string) => {
    const book = books.find((b) => b.isbn === isbn);
    if (book) {
      toast.info(`請在左側表單輸入讀者 ID 完成借出`, {
        description: `ISBN: ${isbn}`,
      });
    }
  };

  const handleQuickReturn = (isbn: string) => {
    handleReturn({ isbn });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Toaster position="top-right" richColors />
      <TopBar />

      <main className="container mx-auto px-6 py-8">
        {/* Two-column layout */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          {/* Left: Transaction Card */}
          <TransactionCard
            onBorrow={handleBorrow}
            onReturn={handleReturn}
            borrowStatus={borrowStatus}
            returnStatus={returnStatus}
          />

          {/* Right: Add Book Form */}
          <AddBookForm onAddBook={handleAddBook} status={addBookStatus} />
        </div>

        {/* Book Table */}
        <BookTable books={books} onQuickBorrow={handleQuickBorrow} onQuickReturn={handleQuickReturn} />
      </main>
    </div>
  );
}

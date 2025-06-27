"use client";

import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';

const Navbar = () => {
  const { isLoggedIn, logout } = useAuth();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <nav className="bg-blue-600 p-4 shadow-md">
      <div className="container mx-auto flex justify-between items-center">
        <button
          onClick={() => router.push(isLoggedIn ? '/dashboard' : '/login')}
          className="text-white text-2xl font-bold cursor-pointer focus:outline-none"
        >
          Mood Predictor
        </button>

        <div className="space-x-4">
          {isLoggedIn ? (
            <>
              <button
                onClick={() => router.push('/')}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold px-4 py-2 rounded-lg transition duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                Home
              </button>
              <button
                onClick={() => router.push('/dashboard')}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold px-4 py-2 rounded-lg transition duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                Dashboard
              </button>
              <button
                onClick={handleLogout}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold px-4 py-2 rounded-lg transition duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <button
                onClick={() => router.push('/')}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold px-4 py-2 rounded-lg transition duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                Home
              </button>
              <button
                onClick={() => router.push('/login')}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold px-4 py-2 rounded-lg transition duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                Login
              </button>
              <button
                onClick={() => router.push('/register')}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold px-4 py-2 rounded-lg transition duration-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                Register
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
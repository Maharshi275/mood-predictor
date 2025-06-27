'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';

interface Question {
  question: string;
  answer_type: 'yes_no' | 'yes_no_maybe';
}

interface Answer {
  question: string;
  answer: string;
}

const DashboardPage = () => {
  const { userId, isLoggedIn, loading: authLoading } = useAuth();
  const router = useRouter();

  const [questions, setQuestions] = useState<Question[]>([]);
  const [answers, setAnswers] = useState<{ [key: number]: string }>({});
  const [currentMood, setCurrentMood] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasCheckedInToday, setHasCheckedInToday] = useState(false);
  const [showMoodResult, setShowMoodResult] = useState(false);

  useEffect(() => {
    if (authLoading) {
      return;
    }
    if (!isLoggedIn) {
      router.push('/login');
      return;
    }

    if (userId && !showMoodResult) {
      fetchQuestions();
    }
  }, [isLoggedIn, userId, authLoading, router, showMoodResult]);

  const fetchQuestions = async () => {
    setLoading(true);
    setError(null);
    setHasCheckedInToday(false);
    setCurrentMood(null);

    try {
      const response = await fetch(`http://localhost:7070/api/checkin/questions/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (response.status === 409) {
        setHasCheckedInToday(true);
        const errorMessage = await response.text();
        setError(errorMessage);
        return;
      }

      if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to fetch questions');
      }

      const data = await response.json();
      const parsedQuestions: Question[] = JSON.parse(data.questionJson);
      setQuestions(parsedQuestions);
      setAnswers({});

    } catch (err: any) {
      setError(err.message || 'An unexpected error occurred while fetching questions.');
      setQuestions([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerChange = (index: number, value: string) => {
    setAnswers(prev => ({
      ...prev,
      [index]: value,
    }));
  };

  const handleSubmitCheckIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setShowMoodResult(false);

    try {
      const questionsJson = JSON.stringify(questions.map(q => ({ question: q.question, answer_type: q.answer_type })));

      const submittedAnswers: Answer[] = questions.map((q, index) => ({
        question: q.question,
        answer: answers[index] || '',
      }));
      const answersJson = JSON.stringify(submittedAnswers);

      const checkInRequest = {
        userId: userId,
        questions: questionsJson,
        answers: answersJson,
      };

      const response = await fetch('http://localhost:7070/api/checkin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(checkInRequest),
      });

      if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to submit check-in');
      }

      const predictedMood = await response.text();
      setCurrentMood(predictedMood);
      setShowMoodResult(true);

    } catch (err: any) {
      setError(err.message || 'An unexpected error occurred during check-in submission.');
      setCurrentMood(null);
      setShowMoodResult(false);
    } finally {
      setLoading(false);
    }
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <p className="text-gray-700 text-lg">Loading...</p>
      </div>
    );
  }

  if (hasCheckedInToday) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100 p-4">
        <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
          <h2 className="text-3xl font-semibold text-gray-800 mb-4">Daily Mood Check-in</h2>
          <p className="text-xl text-yellow-600 font-medium">
            You have already completed a mood check-in for today!
          </p>
          {error && <p className="text-red-500 mt-4">{error}</p>}
        </div>
      </div>
    );
  }

  if (showMoodResult && currentMood) {
    let emoji = '😐';
    let moodColor = 'text-gray-600';
    let bgColor = 'bg-gray-200';
    let shadowColor = 'shadow-gray-400';

    if (currentMood.toLowerCase().includes('happy')) {
      emoji = '😄';
      moodColor = 'text-green-600';
      bgColor = 'bg-green-100';
      shadowColor = 'shadow-green-300';
    } else if (currentMood.toLowerCase().includes('sad')) {
      emoji = '🙁';
      moodColor = 'text-red-600';
      bgColor = 'bg-red-100';
      shadowColor = 'shadow-red-300';
    }

    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
        <div className={`p-8 rounded-lg shadow-xl w-full max-w-md text-center transform transition-all duration-500 scale-100 hover:scale-105 ${bgColor} ${shadowColor}`}>
          <h2 className="text-4xl font-bold mb-4 text-gray-800">Your Mood Today Is:</h2>
          <p className={`text-9xl mb-6`}>
            {emoji}
          </p>
          <p className={`text-5xl font-extrabold ${moodColor} drop-shadow-lg`}>
            {currentMood}
          </p>
          <button
            onClick={() => {
              setShowMoodResult(false);
              setCurrentMood(null);
              setQuestions([]);
              setAnswers({});
              setHasCheckedInToday(false);
              fetchQuestions();
            }}
            className="mt-8 bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-full text-lg shadow-lg hover:shadow-xl transition duration-300 transform hover:scale-105 focus:outline-none focus:ring-4 focus:ring-blue-300"
          >
            Check Tomorrow (or Retry)
          </button>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-xl">
        <h2 className="text-3xl font-semibold text-gray-800 mb-6 text-center">Daily Mood Check-in</h2>

        {error && (
          <p className="text-red-500 text-center text-sm mb-4">{error}</p>
        )}

        <form onSubmit={handleSubmitCheckIn}>
          {questions.length > 0 ? (
            questions.map((q, index) => (
              <div key={index} className="mb-6 p-4 border border-gray-200 rounded-lg bg-gray-50">
                <p className="text-lg font-medium text-gray-800 mb-3">{`${index + 1}. ${q.question}`}</p>
                {q.answer_type === 'yes_no' ? (
                  <div className="flex items-center space-x-6">
                    <label className="inline-flex items-center">
                      <input
                        type="radio"
                        className="form-radio text-blue-600 h-5 w-5"
                        name={`question-${index}`}
                        value="yes"
                        checked={answers[index] === 'yes'}
                        onChange={() => handleAnswerChange(index, 'yes')}
                        required
                      />
                      <span className="ml-2 text-gray-700">Yes</span>
                    </label>
                    <label className="inline-flex items-center">
                      <input
                        type="radio"
                        className="form-radio text-blue-600 h-5 w-5"
                        name={`question-${index}`}
                        value="no"
                        checked={answers[index] === 'no'}
                        onChange={() => handleAnswerChange(index, 'no')}
                        required
                      />
                      <span className="ml-2 text-gray-700">No</span>
                    </label>
                  </div>
                ) : (
                  <div className="flex items-center space-x-6">
                    <label className="inline-flex items-center">
                      <input
                        type="radio"
                        className="form-radio text-blue-600 h-5 w-5"
                        name={`question-${index}`}
                        value="yes"
                        checked={answers[index] === 'yes'}
                        onChange={() => handleAnswerChange(index, 'yes')}
                        required
                      />
                      <span className="ml-2 text-gray-700">Yes</span>
                    </label>
                    <label className="inline-flex items-center">
                      <input
                        type="radio"
                        className="form-radio text-blue-600 h-5 w-5"
                        name={`question-${index}`}
                        value="no"
                        checked={answers[index] === 'no'}
                        onChange={() => handleAnswerChange(index, 'no')}
                        required
                      />
                      <span className="ml-2 text-gray-700">No</span>
                    </label>
                    <label className="inline-flex items-center">
                      <input
                        type="radio"
                        className="form-radio text-blue-600 h-5 w-5"
                        name={`question-${index}`}
                        value="maybe"
                        checked={answers[index] === 'maybe'}
                        onChange={() => handleAnswerChange(index, 'maybe')}
                        required
                      />
                      <span className="ml-2 text-gray-700">Maybe</span>
                    </label>
                  </div>
                )}
              </div>
            ))
          ) : (
            <p className="text-center text-gray-600">No questions available. Please try again.</p>
          )}

          {questions.length > 0 && (
            <div className="flex justify-center mt-6">
              <button
                type="submit"
                className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-lg text-lg shadow-md hover:shadow-lg transition duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-75"
                disabled={loading || Object.keys(answers).length !== questions.length}
              >
                {loading ? 'Submitting...' : 'Submit Check-in'}
              </button>
            </div>
          )}
        </form>
      </div>
    </div>
  );
};

export default DashboardPage;

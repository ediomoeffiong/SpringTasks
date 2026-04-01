import React from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from 'axios';

// --- API Client ---
const apiClient = axios.create({
  baseURL: '/api/dashboard',
  // You can attach interceptors here to automatically include the JWT token
});

// --- Types ---
interface DashboardStats {
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  overdueTasks: number;
  completionRate: number;
}

interface Task {
  id: number;
  title: string;
  description: string;
  dueDate: string;
  completed: boolean;
  priority: string;
}

interface CategoryBreakdown {
  categoryName: string;
  taskCount: number;
  completionRate: number;
}

// --- Components ---

const StatCard = ({ title, value, icon, trend }: { title: string, value: string | number, icon: React.ReactNode, trend?: 'up' | 'down' | 'none' }) => (
  <div className="p-6 bg-white rounded-2xl shadow-sm border border-gray-100 flex items-center justify-between transition-transform hover:-translate-y-1">
    <div>
      <p className="text-sm font-medium text-gray-500 mb-1">{title}</p>
      <div className="flex items-end gap-2">
        <h3 className="text-3xl font-bold text-gray-900">{value}</h3>
        {trend && trend !== 'none' && (
          <span className={`text-sm font-medium ${trend === 'up' ? 'text-green-500' : 'text-red-500'}`}>
            {trend === 'up' ? '↑' : '↓'}
          </span>
        )}
      </div>
    </div>
    <div className="p-4 bg-indigo-50 text-indigo-600 rounded-xl">
      {icon}
    </div>
  </div>
);

const SkeletonCard = () => (
  <div className="p-6 bg-white rounded-2xl shadow-sm border border-gray-100 animate-pulse">
    <div className="h-4 bg-gray-200 rounded w-1/2 mb-4"></div>
    <div className="h-8 bg-gray-200 rounded w-1/3"></div>
  </div>
);

export default function Dashboard() {
  // --- Data Fetching Hooks (React Query) ---
  const { data: stats, isLoading: statsLoading } = useQuery<DashboardStats>({
    queryKey: ['dashboard', 'stats'],
    queryFn: async () => (await apiClient.get('/stats')).data,
  });

  const { data: recentTasks = [], isLoading: recentLoading } = useQuery<Task[]>({
    queryKey: ['dashboard', 'recentTasks'],
    queryFn: async () => (await apiClient.get('/recent-tasks')).data,
  });

  const { data: upcomingEvents = [], isLoading: upcomingLoading } = useQuery<Task[]>({
    queryKey: ['dashboard', 'upcomingEvents'],
    queryFn: async () => (await apiClient.get('/upcoming-events')).data,
  });

  const { data: categories = [], isLoading: categoriesLoading } = useQuery<CategoryBreakdown[]>({
    queryKey: ['dashboard', 'categories'],
    queryFn: async () => (await apiClient.get('/category-breakdown')).data,
  });

  // --- Render Functions ---
  const isDataLoading = statsLoading || recentLoading || upcomingLoading || categoriesLoading;

  return (
    <div className="min-h-screen bg-gray-50/50 p-8 font-sans">
      
      {/* Header Section */}
      <header className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Good morning, Ediomo</h1>
          <p className="text-gray-500 mt-1">Here's what's happening with your tasks today.</p>
        </div>
        <button 
          className="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-lg font-medium tracking-wide shadow-sm transition-colors flex items-center gap-2"
          onClick={() => alert('Open Task Modal logic')}
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4"></path></svg>
          New Task
        </button>
      </header>

      {/* Stats Overview */}
      <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {statsLoading ? (
          <>
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </>
        ) : (
          <>
            <StatCard 
              title="Total Tasks" 
              value={stats?.totalTasks || 0} 
              icon={<svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>} 
            />
            <StatCard 
              title="Completed" 
              value={stats?.completedTasks || 0} 
              trend="up"
              icon={<svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path></svg>} 
            />
            <StatCard 
              title="Pending" 
              value={stats?.pendingTasks || 0} 
              icon={<svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>} 
            />
            <StatCard 
              title="Success Rate" 
              value={`${stats?.completionRate || 0}%`} 
              icon={<svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>} 
            />
          </>
        )}
      </section>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left Column: Recent Tasks (Takes up 2 cols) */}
        <section className="lg:col-span-2 space-y-8">
          
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-gray-900">Recent Tasks</h2>
              <a href="/tasks" className="text-indigo-600 hover:text-indigo-800 text-sm font-medium">View All</a>
            </div>
            
            {recentLoading ? (
              <div className="space-y-4">
                 <SkeletonCard /><SkeletonCard />
              </div>
            ) : recentTasks.length === 0 ? (
              <div className="text-center py-10">
                <p className="text-gray-500">No recent tasks available.</p>
              </div>
            ) : (
              <div className="space-y-4">
                {recentTasks.map((task: Task) => (
                  <div key={task.id} className="group p-4 bg-gray-50 hover:bg-indigo-50/50 rounded-xl flex items-center justify-between transition-colors border border-transparent hover:border-indigo-100">
                    <div className="flex items-center gap-4">
                      {/* Checkbox circle */}
                      <button className={`w-6 h-6 rounded-full border-2 flex items-center justify-center transition-colors ${task.completed ? 'bg-indigo-500 border-indigo-500' : 'border-gray-300 hover:border-indigo-400'}`}>
                        {task.completed && <svg className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7"></path></svg>}
                      </button>
                      <div>
                        <h4 className={`font-medium ${task.completed ? 'line-through text-gray-400' : 'text-gray-900'}`}>{task.title}</h4>
                        {task.dueDate && <p className="text-xs text-gray-500 mt-1">Due: {new Date(task.dueDate).toLocaleDateString()}</p>}
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-3">
                      <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${task.priority === 'High' ? 'bg-red-100 text-red-700' : 'bg-gray-200 text-gray-700'}`}>
                        {task.priority || 'Normal'}
                      </span>
                      <button className="text-gray-400 hover:text-indigo-600 p-1 opacity-0 group-hover:opacity-100 transition-opacity">
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Right Column: Upcoming & Categories */}
        <section className="space-y-8">
          
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-6">Upcoming This Week</h2>
            {upcomingLoading ? (
              <SkeletonCard />
            ) : upcomingEvents.length === 0 ? (
              <div className="text-center py-6">
                <p className="text-sm text-gray-500">Your week is clear!</p>
              </div>
            ) : (
              <div className="space-y-3">
                {upcomingEvents.slice(0,4).map((event: Task) => (
                  <div key={event.id} className="flex gap-4 p-3 rounded-xl border border-gray-100 items-start">
                    <div className="bg-indigo-50 text-indigo-700 rounded-lg p-2 text-center min-w-[3rem]">
                      <span className="block text-xs font-bold uppercase">{new Date(event.dueDate).toLocaleDateString('en-US', { weekday: 'short' })}</span>
                      <span className="block text-lg font-black">{new Date(event.dueDate).getDate()}</span>
                    </div>
                    <div>
                      <h5 className="font-semibold text-gray-900 text-sm">{event.title}</h5>
                      <span className="text-xs text-gray-500 mt-0.5 inline-block">{event.completed ? 'Done' : 'Pending'}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
          
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-xl font-bold text-gray-900 mb-6">Category Breakdown</h2>
            {categoriesLoading ? (
              <SkeletonCard />
            ) : categories.length === 0 ? (
              <div className="text-center py-6 text-sm text-gray-500">No categories found.</div>
            ) : (
              <div className="space-y-4">
                {categories.map((cat: CategoryBreakdown, i: number) => (
                  <div key={i}>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="font-medium text-gray-700">{cat.categoryName}</span>
                      <span className="text-gray-500">{cat.taskCount} tasks ({cat.completionRate.toFixed(0)}%)</span>
                    </div>
                    <div className="w-full bg-gray-100 rounded-full h-2">
                      <div className="bg-indigo-500 h-2 rounded-full" style={{ width: `${cat.completionRate}%` }}></div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

        </section>
      </div>
    </div>
  );
}

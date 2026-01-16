interface ComingSoonProps {
    title: string;
    description?: string;
  }
  
  export function ComingSoon({ title, description }: ComingSoonProps) {
    return (
      <div className="max-w-6xl">
        <div className="mb-6">
          <h1 className="text-xl font-semibold text-gray-900">{title}</h1>
          {description && <p className="text-sm text-gray-500 mt-1">{description}</p>}
        </div>
  
        <div className="card">
          <div className="flex flex-col items-center justify-center py-12">
            <svg
              className="w-16 h-16 text-gray-300 mb-4"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            <p className="text-gray-500 text-sm">この画面は準備中です</p>
            {description && (
              <p className="text-gray-400 text-xs mt-2">{description}</p>
            )}
          </div>
        </div>
      </div>
    );
  }
import { Request, Response, NextFunction, RequestHandler } from 'express';

/**
 * Wrap an async route handler so any rejected promise is forwarded to next(err).
 * This avoids returning a Promise where a void return was expected by the caller
 * (and satisfies @typescript-eslint/no-misused-promises lint rule).
 */
export const asyncHandler = (
  fn: (req: Request, res: Response, next: NextFunction) => Promise<any>
): RequestHandler => {
  return (req: Request, res: Response, next: NextFunction) => {
    // ensure we catch any rejection and pass to next()
    Promise.resolve(fn(req, res, next)).catch(next);
  };
};

export default asyncHandler;

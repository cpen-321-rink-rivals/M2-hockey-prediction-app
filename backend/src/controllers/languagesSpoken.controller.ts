import { NextFunction, Request, Response } from 'express';

import { LANGUAGES_SPOKEN } from '../languagesSpoken';
import logger from '../logger.util';
import { getAllLanguagesSpokenResponse } from '../types/languagesSpoken.types';

export class LanguagesSpokenController {
  getAllLanguagesSpoken(
    req: Request,
    res: Response<getAllLanguagesSpokenResponse>,
    next: NextFunction
  ) {
    try {
      res.status(200).json({
        message: 'All languages spoken fetched successfully',
        data: { languagesSpoken: LANGUAGES_SPOKEN },
      });
    } catch (error) {
      logger.error('Failed to fetch available languages spoken:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message:
            error.message || 'Failed to fetch available languages spoken',
        });
      }

      next(error);
    }
  }
}

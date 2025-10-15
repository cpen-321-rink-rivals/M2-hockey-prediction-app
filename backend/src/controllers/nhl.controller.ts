// src/controllers/nhl.controller.ts
import { Request, Response, NextFunction } from 'express';
import nhlService from '../nhl.service';

export const getGamesByDate = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const date = (req.query.date as string) || new Date().toISOString().slice(0, 10);
    const data = await nhlService.getGamesByDate(date);
    res.status(200).json({ date, ...data });
  } catch (err) {
    next(err);
  }
};

export const getTeams = async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const data = await nhlService.getTeams();
    res.status(200).json(data);
  } catch (err) {
    next(err);
  }
};

export const getStandings = async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const data = await nhlService.getStandings();
    res.status(200).json(data);
  } catch (err) {
    next(err);
  }
};

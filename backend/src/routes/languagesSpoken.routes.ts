import { Router } from 'express';
import { LanguagesSpokenController } from '../controllers/languagesSpoken.controller';

const router = Router();
const languagesSpokenController = new LanguagesSpokenController();

router.get('/', languagesSpokenController.getAllLanguagesSpoken);

export default router;

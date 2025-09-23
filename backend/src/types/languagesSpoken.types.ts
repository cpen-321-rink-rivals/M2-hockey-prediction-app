import { LANGUAGES_SPOKEN } from '../languagesSpoken';

export type getAllLanguagesSpokenResponse = {
  message: string;
  data?: {
    languagesSpoken: typeof LANGUAGES_SPOKEN;
  };
};

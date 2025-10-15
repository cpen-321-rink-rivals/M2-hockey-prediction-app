// src/nhl.service.ts
import axios from 'axios';

const NHL_API = 'https://statsapi.web.nhl.com/api/v1';

export class NHLService {
  async getGamesByDate(dateISO: string) {
    // date format: YYYY-MM-DD
    const { data } = await axios.get(`${NHL_API}/schedule`, {
      params: { date: dateISO },
      // timeout keeps local dev snappy; adjust as you like
      timeout: 10_000,
    });
    return data;
  }

  async getTeams() {
    const { data } = await axios.get(`${NHL_API}/teams`, { timeout: 10_000 });
    return data;
  }

  async getStandings() {
    const { data } = await axios.get(`${NHL_API}/standings`, { timeout: 10_000 });
    return data;
  }
}

export default new NHLService();

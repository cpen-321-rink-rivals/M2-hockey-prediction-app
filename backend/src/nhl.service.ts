import axios from 'axios';

const NHL_WEB = 'https://api-web.nhle.com/v1';

const http = axios.create({
  baseURL: NHL_WEB,
  timeout: 10_000,
  headers: { Accept: 'application/json', 'User-Agent': 'm3-hockey-app/1.0' },
});

export class NHLService {
  /** GET /v1/schedule/{YYYY-MM-DD} */
  async getSchedule(dateISO: string) {
    const { data } = await http.get(`/schedule/${dateISO}`);
    return data;
  }
}

export default new NHLService();

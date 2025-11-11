export type BingoTicketScore = {
  noCrossedOff: number;
  noRows: number;
  noColumns: number;
  noCrosses: number;
  total: number;
};

/**
 * Compute Bingo ticket score given crossedOff boolean array of length 9.
 * Layout indices:
 * 0 1 2
 * 3 4 5
 * 6 7 8
 */

/**
 * Normalize input to a strict boolean array of length 9.
 */
function normalizeCrossedOff(input: unknown): boolean[] {
  const arr: boolean[] = new Array(9).fill(false);
  if (!Array.isArray(input)) return arr;
  for (let i = 0; i < 9; i++) {
    arr[i] = !!(input as any)[i];
  }
  return arr;
}

/**
 * Count fully crossed rows (3 rows).
 */
function countRows(arr: boolean[]): number {
  let count = 0;
  for (let r = 0; r < 3; r++) {
    const start = r * 3;
    if (arr[start] && arr[start + 1] && arr[start + 2]) count++;
  }
  return count;
}

/**
 * Count fully crossed columns (3 columns).
 */
function countColumns(arr: boolean[]): number {
  let count = 0;
  for (let c = 0; c < 3; c++) {
    if (arr[c] && arr[c + 3] && arr[c + 6]) count++;
  }
  return count;
}

/**
 * Count crossed diagonals (2 possible).
 */
function countDiagonals(arr: boolean[]): number {
  let count = 0;
  if (arr[0] && arr[4] && arr[8]) count++;
  if (arr[2] && arr[4] && arr[6]) count++;
  return count;
}

export function computeTicketScore(crossedOff: boolean[]): BingoTicketScore {
  const arr = normalizeCrossedOff(crossedOff);

  const noCrossedOff = arr.reduce((sum, v) => sum + (v ? 1 : 0), 0);
  const noRows = countRows(arr);
  const noColumns = countColumns(arr);
  const noCrosses = countDiagonals(arr);

  // scoring rules
  const perSquare = noCrossedOff * 1;
  const perLine = (noRows + noColumns + noCrosses) * 3;
  const bingoBonus = noCrossedOff === 9 ? 10 : 0;

  const total = perSquare + perLine + bingoBonus;

  return {
    noCrossedOff,
    noRows,
    noColumns,
    noCrosses,
    total,
  };
}

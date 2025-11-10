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
export function computeTicketScore(crossedOff: boolean[]): BingoTicketScore {
  const arr = crossedOff.slice(0,9);
  const noCrossedOff = arr.filter(Boolean).length;

  let noRows = 0;
  let noColumns = 0;
  let noCrosses = 0;

  // rows
  for (let r = 0; r < 3; r++) {
    const start = r * 3;
    if (arr[start] && arr[start+1] && arr[start+2]) noRows++;
  }

  // columns
  for (let c = 0; c < 3; c++) {
    if (arr[c] && arr[c+3] && arr[c+6]) noColumns++;
  }

  // diagonals
  if (arr[0] && arr[4] && arr[8]) noCrosses++;
  if (arr[2] && arr[4] && arr[6]) noCrosses++;

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

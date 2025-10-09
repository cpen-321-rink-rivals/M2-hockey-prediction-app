export interface IChallenge {
  id: string;
  title: string;
  ownerId: string;
  members: string[];
  createdAt: Date;
  updatedAt: Date;
}

export interface Notification {
  id: number;
  actorUsername: string;
  type: NotificationType;
  message: string;
  postId?: number;
  commentId?: number;
  isRead: boolean;
  createdAt: Date;
}

export enum NotificationType {
  FOLLOW = 'FOLLOW',
  LIKE = 'LIKE',
  COMMENT = 'COMMENT',
  NEW_POST = 'NEW_POST',
  REPORT_REVIEWED = 'REPORT_REVIEWED'
}

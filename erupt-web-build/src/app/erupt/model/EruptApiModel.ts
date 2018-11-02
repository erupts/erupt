/**
 * Created by liyuepeng on 11/1/18.
 */
export interface EruptApiModel {
  data: any;
  status: Status;
}

export enum Status {
  SUCCESS = "SUCCESS",
  ERROR = "ERROR",
  NO_LOGIN = "NO_LOGIN",
  NO_RIGHT = "NO_RIGHT"
}


/**
 * Created by liyuepeng on 10/24/18.
 */
export interface Page {
  total: number;
  pageNumber: number;
  pageSize: number;
  list?: Array<object>;
}

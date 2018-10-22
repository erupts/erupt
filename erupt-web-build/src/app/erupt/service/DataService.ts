/**
 * Created by liyuepeng on 10/17/18.
 */
import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EruptModel} from "../model/erupt.model";
import {Observable} from "rxjs/Observable";

@Injectable()
export class DataService {

  domian: string = window["domian"];

  constructor(private http: HttpClient) {
  }

  getEruptBuild(model): Observable<EruptModel> {
    return this.http.get<EruptModel>(this.domian + '/erupt-api/build/list/' + model);
  }
}

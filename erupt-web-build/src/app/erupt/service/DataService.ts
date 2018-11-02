/**
 * Created by liyuepeng on 10/17/18.
 */
import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EruptModel} from "../model/erupt.model";
import {Observable} from "rxjs/Observable";
import {Page} from "../model/page";

@Injectable()
export class DataService {

  domain: string = window["domain"];


  constructor(private http: HttpClient) {
  }

  getEruptBuild(model): Observable<EruptModel> {
    return this.http.get<EruptModel>(this.domain + '/erupt-api/build/list/' + model);
  }

  queryEruptData(model): Observable<Page> {
    return this.http.get<Page>(this.domain + '/erupt-api/data/' + model);
  }

  // deleteEruptData(model, id): Observable {
  //   return this.http.delete(this.domain + '/erupt-api/data/' + model + "/1");
  // }
}

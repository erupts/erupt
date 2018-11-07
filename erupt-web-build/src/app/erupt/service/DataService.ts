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

  eruptHeaderKey: String = "erupt";


  constructor(private http: HttpClient) {
  }

  getEruptBuild(model): Observable<EruptModel> {
    return this.http.get<EruptModel>(this.domain + '/erupt-api/build/list/' + model, {
      headers: {
        eruptHeaderKey: model
      }
    });
  }

  queryEruptData(model): Observable<Page> {
    return this.http.get<Page>(this.domain + '/erupt-api/data/' + model, {
      headers: {
        eruptHeaderKey: model
      }
    });
  }

  queryEruptReferenceData(model, refName): Observable<any> {
    return this.http.get(this.domain + "/erupt-api/data/" + model + "/ref/" + refName, {
      headers: {
        eruptHeaderKey: model
      }
    });
  }

  addEruptData(model, data: any): Observable<any> {
    return this.http.post(this.domain + "/erupt-api/data/" + model, data, {
      headers: {
        eruptHeaderKey: model
      }
    });
  }

  deleteEruptData(model, id): Observable<any> {
    return this.http.delete(this.domain + '/erupt-api/data/' + model + "/" + id);
  }
}

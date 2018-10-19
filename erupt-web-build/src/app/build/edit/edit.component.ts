import {Component, OnInit} from '@angular/core';
import {EruptModel} from "../../erupt/model/erupt.model";
import {DataService} from "../../erupt/service/DataService";

@Component({
  selector: 'erupt-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.scss']
})
export class EditComponent implements OnInit {

  public eruptModel: EruptModel;

  constructor(private data: DataService) {

  }

  ngOnInit() {
    this.data.getEruptBuild("mmo").subscribe(
      em => {
        this.eruptModel = em;
        console.log(this.eruptModel);
      },
      error => {
        console.log(error);
      }
    );

  }

}

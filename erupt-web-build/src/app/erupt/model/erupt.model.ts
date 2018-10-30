import {EruptFieldModel} from "./erupt-field.model";
import {RgbColor} from "./erupt.enum";
/**
 * Created by liyuepeng on 10/16/18.
 */
export interface EruptModel {
  eruptFieldModels: Array<EruptFieldModel>;
  eruptJson: Erupt;
  eruptName: string;
}

export interface Erupt {
  name: string;
  power: Power;
  cards: Array<Card>;
}

interface Power {
  add: boolean;
  del: boolean;
  edit: boolean;
  query: boolean;
  export: boolean;
  importable: boolean;
}

interface Card {
  icon: string;
  value: string;
  desc: string;
  color: RgbColor;
}

"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
var card_animation_1 = require("./card-animation");
var CardComponent = (function () {
    function CardComponent() {
        this.cardToggle = 'expanded';
        this.cardClose = 'open';
    }
    CardComponent.prototype.ngOnInit = function () {
    };
    CardComponent.prototype.toggleCard = function () {
        this.cardToggle = this.cardToggle === 'collapsed' ? 'expanded' : 'collapsed';
    };
    CardComponent.prototype.closeCard = function () {
        this.cardClose = this.cardClose === 'closed' ? 'open' : 'closed';
    };
    return CardComponent;
}());
__decorate([
    core_1.Input()
], CardComponent.prototype, "headerContent", void 0);
__decorate([
    core_1.Input()
], CardComponent.prototype, "title", void 0);
__decorate([
    core_1.Input()
], CardComponent.prototype, "blockClass", void 0);
__decorate([
    core_1.Input()
], CardComponent.prototype, "cardClass", void 0);
CardComponent = __decorate([
    core_1.Component({
        selector: 'app-card',
        templateUrl: './card.component.html',
        styleUrls: ['./card.component.scss'],
        animations: [card_animation_1.cardToggle, card_animation_1.cardClose],
        encapsulation: core_1.ViewEncapsulation.None
    })
], CardComponent);
exports.CardComponent = CardComponent;

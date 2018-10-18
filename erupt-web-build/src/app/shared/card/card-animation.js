"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var animations_1 = require("@angular/animations");
exports.cardToggle = animations_1.trigger('cardToggle', [
    animations_1.state('collapsed, void', animations_1.style({
        overflow: 'hidden',
        height: '0px',
    })),
    animations_1.state('expanded', animations_1.style({
        height: animations_1.AUTO_STYLE,
    })),
    animations_1.transition('collapsed <=> expanded', [
        animations_1.animate('400ms ease-in-out')
    ])
]);
exports.cardClose = animations_1.trigger("cardClose", [
    animations_1.state("open", animations_1.style({
        opacity: 1
    })),
    animations_1.state("closed", animations_1.style({
        opacity: 0,
        display: 'none'
    })),
    animations_1.transition("open <=> closed", animations_1.animate("400ms")),
]);

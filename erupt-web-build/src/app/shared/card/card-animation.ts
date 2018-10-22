import {trigger, state, style, AUTO_STYLE, transition, animate} from '@angular/animations';

export const cardToggle = trigger('cardToggle', [
  state('collapsed, void',
    style({
      overflow: 'hidden',
      height: '0px',
    })
  ),
  state('expanded',
    style({
      overflow: 'hidden',
      height: AUTO_STYLE,
    })
  ),
  transition('collapsed <=> expanded', [
    animate('400ms ease-in-out')
  ])
]);

export const cardClose = trigger('cardClose', [
  state('open', style({
    opacity: 1
  })),
  state('closed', style({
    opacity: 0,
    display: 'none'
  })),
  transition('open <=> closed', animate('400ms')),
]);

export const cardIconToggle = trigger('cardIconToggle', [
  state('an-off, void',
    style({
      overflow: 'hidden',
      width: '35px',
    })
  ),
  state('an-animate',
    style({
      overflow: 'hidden',
      width: AUTO_STYLE,
    })
  ),
  transition('an-off <=> an-animate', [
    animate('400ms ease-in-out')
  ])
]);

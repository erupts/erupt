import {Injectable} from '@angular/core';

export interface BadgeItem {
  type: string;
  value: string;
}

export interface ChildrenItems {
  state: string;
  target?: boolean;
  name: string;
  type?: string;
  children?: ChildrenItems[];
}

export interface MainMenuItems {
  state: string;
  short_label?: string;
  main_state?: string;
  target?: boolean;
  name: string;
  type: string;
  icon: string;
  badge?: BadgeItem[];
  children?: ChildrenItems[];
}

export interface Menu {
  label: string;
  main: MainMenuItems[];
}

const MENUITEMS = [
  {
    label: 'Erupt Pages',
    main: [
      {
        state: 'list',
        short_label: 'L',
        name: 'list',
        type: 'sub',
        icon: 'feather icon-home',
        children: [
          {
            state: 'edit',
            name: 'edit'
          },
          {
            state: 'table',
            name: 'table'
          }
        ]
      }
    ],
  },
  {
    label: 'Auth',
    main: [
      {
        state: 'auth/login/simple',
        short_label: 'L',
        name: 'auth',
        type: 'link',
        icon: 'feather icon-home'
      }
    ],
  },
  {
    label: 'Navigation',
    main: [
      {
        state: 'dashboard',
        short_label: 'D',
        name: 'Dashboard',
        type: 'sub',
        icon: 'feather icon-home',
        children: [
          {
            state: 'default',
            name: 'Default'
          },
          {
            state: 'ecommerce',
            name: 'Ecommerce'
          },
          {
            state: 'crm-dashboard',
            name: 'CRM'
          },
          {
            state: 'analytics',
            name: 'Analytics',
            badge: [
              {
                type: 'info',
                value: 'NEW'
              }
            ]
          },
          {
            state: 'project',
            name: 'Project'
          }
        ]
      },
      {
        state: 'navigation',
        short_label: 'N',
        name: 'Navigation',
        type: 'link',
        icon: 'feather icon-menu'
      },
      {
        state: 'widget',
        short_label: 'W',
        name: 'Widget',
        type: 'sub',
        icon: 'feather icon-layers',
        badge: [
          {
            type: 'danger',
            value: '100+'
          }
        ],
        children: [
          {
            state: 'statistic',
            name: 'Statistic'
          },
          {
            state: 'data',
            name: 'Data'
          },
          {
            state: 'chart',
            name: 'Chart'
          },
          {
            state: 'advanced',
            name: 'Advance'
          }
        ]
      }
    ],
  }
];


@Injectable()
export class MenuItems {
  getAll(): Menu[] {
    return MENUITEMS;
  }
}

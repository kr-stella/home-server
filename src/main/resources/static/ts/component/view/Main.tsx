const NODE_MODE = process.env.NODE_ENV;

import axios from "axios";
import isEmpty from "lodash/isEmpty";
import React, { Fragment, lazy, useCallback, useRef, useState } from "react";

import { ClickEvent } from "../../config/type";
import { progressError, progressWarning } from "../common/alert";
import classNames from "classnames";

const DarkMode = lazy(() => import("../common/DarkMode"));

const InputID = lazy(() => import("../common/input").then(module => ({ default: module.String })));
const InputPassword = lazy(() => import("../common/input").then(module => ({ default: module.Password })));

const doc = window.document;
const Main = () => {

	/** 상태값 */
	const ref = useRef<HTMLParagraphElement>(null);
	const timeout = useRef<number |null>(null);

	const [ username, setUsername ] = useState<string>(``);
	const [ password, setPassword ] = useState<string>(``);
	const [ rememberMe, setRememberMe ] = useState<boolean>(false);

	/** 아이콘 클릭 시 Focus */
	const onClick = useCallback((e:ClickEvent) => {

		const target = e.currentTarget.parentNode as HTMLElement;
		Array.from(doc.getElementsByClassName(`login-input`)).map(v => {
			v.classList.remove(`on`)
		});
		
		if(target) {
			(target.lastChild as HTMLElement).focus();
			target.classList.add("on");
		}

	}, []);

	/** CapsLock 여부 */
	const onCapsLock = useCallback((isActive:boolean) => {
		if(ref.current) {

			clearTimeout(timeout.current ?? undefined);
			ref.current.style.display = isActive? `block`:`none`;

			if(isActive) {
				timeout.current = window.setTimeout(() => {
					if(ref.current)
						ref.current.style.display = `none`;
				}, 1500);
			}

		}
	}, []);
	
	/** 인수영역에 data( 상태값 )세팅을 하지 않으면 useEffect에서 찍어봤을 때 변경되지 않음. */
	const onId = useCallback((v:string) => {
		setUsername(v);
	}, [username]);
	const onPassword = useCallback((v:string) => {
		setPassword(v);
	}, [password]);
	const onRememberMe = useCallback(() => {
		setRememberMe(!rememberMe);
	}, [rememberMe]);

	/** JTI 생성 ( JWT Token ID - 기기의 고유 식별번호 생성을 위함. ) */
	const createJTI = useCallback(() => {
		return `xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx`.replace(/[xy]/g, function(c) {
			let a = Math.random() * 16 | 0, v = c === `x` ? a : (a & 0x3 | 0x8);
			return v.toString(16);
		});
	}, []);
	
	/** 로그인 Action */
	const onLogin = useCallback((e:React.FormEvent<HTMLFormElement>) => {

		/** 새로고침 방지 */
		e.preventDefault();
		e.stopPropagation();

		/** 데이터 */
		if(!username)
			{ progressWarning(`ID를 입력해주세요.`); return; }
		if(!password)
			{ progressWarning(`비밀번호를 입력해주세요.`); return; }
		/** 기기의 고유 식별번호 */
		if(!localStorage.getItem(`device`))
			localStorage.setItem(`device`, createJTI());

		const data = new FormData();
		data.append(`username`, username);
		data.append(`password`, password);
		data.append(`device`, localStorage.getItem(`device`) || ``);
		data.append(`rememberMe`, rememberMe.toString()); // Remember Me 상태 추가
		axios(`/loginproc`, {
			method: `POST`, data,
			headers: {
				"Content-Type": `application/x-www-form-urlencoded`
			}
		}).then((res) => {

			const { redirect } = res.data;
			if(res.status === 200)
				window.location.href = redirect;

		}).catch((err) => {

			if(!isEmpty(err.response.data.str)) progressError(err.response.data.str)
			else progressError(`서버 및 데이터베이스 이슈발생 <br /> 관리자에게 연락부탁드립니다.`);

		});

	}, [ username, password, rememberMe ]);

	const [ noti, setNoti ] = useState<boolean>(true);
	return (
	<Fragment>
		<p>{`로그인 성공했습니다.`}</p>
		{NODE_MODE && <DarkMode mode={NODE_MODE} />}
	</Fragment>
	);

};

export default React.memo(Main);